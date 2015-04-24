package de.peeeq.jmpq.adpcm;

import java.nio.ByteBuffer;
import java.util.Optional;

public class Adpcm {

	private static final int MAX_ADPCM_CHANNEL_COUNT = 2;
	private static final int INITIAL_ADPCM_STEP_INDEX = 0x2C;

	private static int NextStepTable[] = { -1, 0, -1, 4, -1, 2, -1, 6, -1, 1, -1, 5, -1, 3, -1, 7, -1, 1, -1, 5, -1, 3, -1, 7,
			-1, 2, -1, 4, -1, 6, -1, 8 };

	private static int StepSizeTable[] = { 7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55,
			60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494,
			544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024,
			3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 15289,
			16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767 };

	private static short GetNextStepIndex(int StepIndex, @Unsigned int EncodedSample) {
		// Get the next step index
		StepIndex = StepIndex + NextStepTable[EncodedSample & 0x1F];

		// Don't make the step index overflow
		if (StepIndex < 0)
			StepIndex = 0;
		else if (StepIndex > 88)
			StepIndex = 88;

		return (short) StepIndex;
	}

	private static int UpdatePredictedSample(int PredictedSample, int EncodedSample, int Difference) {
		// Is the sign bit set?
		if ((EncodedSample & 0x40) != 0) {
			PredictedSample -= Difference;
			if (PredictedSample <= -32768)
				PredictedSample = -32768;
		} else {
			PredictedSample += Difference;
			if (PredictedSample >= 32767)
				PredictedSample = 32767;
		}

		return PredictedSample;
	}

	private static int DecodeSample(int PredictedSample, int EncodedSample, int StepSize, int Difference) {
		if ((EncodedSample & 0x01) != 0)
			Difference += (StepSize >> 0);

		if ((EncodedSample & 0x02) != 0)
			Difference += (StepSize >> 1);

		if ((EncodedSample & 0x04) != 0)
			Difference += (StepSize >> 2);

		if ((EncodedSample & 0x08) != 0)
			Difference += (StepSize >> 3);

		if ((EncodedSample & 0x10) != 0)
			Difference += (StepSize >> 4);

		if ((EncodedSample & 0x20) != 0)
			Difference += (StepSize >> 5);

		return UpdatePredictedSample(PredictedSample, EncodedSample, Difference);
	}

	public int compressADPCM(ByteBuffer pvOutBuffer, ByteBuffer pvInBuffer, int ChannelCount, int CompressionLevel) {
		// The output stream
		final TADPCMStream os = new TADPCMStream(pvOutBuffer);
		// The input stream
		final TADPCMStream is = new TADPCMStream(pvInBuffer);
		final byte BitShift = (byte) (CompressionLevel - 1);
		// Predicted samples for each channel
		final short[] PredictedSamples = new short[MAX_ADPCM_CHANNEL_COUNT];
		// Step indexes for each channel
		final short[] StepIndexes = new short[MAX_ADPCM_CHANNEL_COUNT];

		// _tprintf(_T("== CMPR Started ==============\n"));

		// First byte in the output stream contains zero. The second one
		// contains the compression level
		os.WriteByteSample((byte) 0);
		if (!os.WriteByteSample(BitShift))
			return 2;

		// Set the initial step index for each channel
		StepIndexes[0] = StepIndexes[1] = INITIAL_ADPCM_STEP_INDEX;

		// Next, InitialSample value for each channel follows
		for (int i = 0; i < ChannelCount; i++) {
			// Get the initial sample from the input stream
			final Optional<Short> InputSampleOpt = is.ReadWordSample();
			if (!InputSampleOpt.isPresent())
				return os.LengthProcessed();
			final short InputSample = InputSampleOpt.get();
			// Store the initial sample to our sample array
			PredictedSamples[i] = InputSample;

			// Also store the loaded sample to the output stream
			if (!os.WriteWordSample(InputSample))
				return os.LengthProcessed();
		}

		// Get the initial index
		int ChannelIndex = ChannelCount - 1;

		// Now keep reading the input data as long as there is something in the
		// input buffer
		while (true) {
			final Optional<Short> InputSampleOpt = is.ReadWordSample();
			if (!InputSampleOpt.isPresent())
				break;
			final short InputSample = InputSampleOpt.get();

			int EncodedSample = 0;

			// If we have two channels, we need to flip the channel index
			ChannelIndex = (ChannelIndex + 1) % ChannelCount;

			// Get the difference from the previous sample.
			// If the difference is negative, set the sign bit to the encoded
			// sample
			int AbsDifference = InputSample - PredictedSamples[ChannelIndex];
			if (AbsDifference < 0) {
				AbsDifference = -AbsDifference;
				EncodedSample |= 0x40;
			}

			// If the difference is too low (higher that difference treshold),
			// write a step index modifier marker
			int StepSize = StepSizeTable[StepIndexes[ChannelIndex]];
			if (AbsDifference < (StepSize >> CompressionLevel)) {
				if (StepIndexes[ChannelIndex] != 0)
					StepIndexes[ChannelIndex]--;

				os.WriteByteSample((byte) 0x80);
			} else {
				// If the difference is too high, write marker that
				// indicates increase in step size
				while (AbsDifference > (StepSize << 1)) {
					if (StepIndexes[ChannelIndex] >= 0x58)
						break;

					// Modify the step index
					StepIndexes[ChannelIndex] += 8;
					if (StepIndexes[ChannelIndex] > 0x58)
						StepIndexes[ChannelIndex] = 0x58;

					// Write the "modify step index" marker
					StepSize = StepSizeTable[StepIndexes[ChannelIndex]];
					os.WriteByteSample((byte) 0x81);
				}

				// Get the limit bit value
				int MaxBitMask = (1 << (BitShift - 1));
				MaxBitMask = (MaxBitMask > 0x20) ? 0x20 : MaxBitMask;
				final int Difference = StepSize >> BitShift;
				int TotalStepSize = 0;

				for (int BitVal = 0x01; BitVal <= MaxBitMask; BitVal <<= 1) {
					if ((TotalStepSize + StepSize) <= AbsDifference) {
						TotalStepSize += StepSize;
						EncodedSample |= BitVal;
					}
					StepSize >>= 1;
				}

				PredictedSamples[ChannelIndex] = (short) UpdatePredictedSample(PredictedSamples[ChannelIndex],
						EncodedSample, Difference + TotalStepSize);
				// Write the encoded sample to the output stream
				if (!os.WriteByteSample((byte) EncodedSample))
					break;

				// Calculates the step index to use for the next encode
				StepIndexes[ChannelIndex] = GetNextStepIndex(StepIndexes[ChannelIndex], EncodedSample);
			}
		}

		// _tprintf(_T("== CMPR Ended ================\n"));
		return os.LengthProcessed();
	}

	public int decompressADPCM(ByteBuffer pvOutBuffer, ByteBuffer pvInBuffer, int ChannelCount) {
		// Output stream
		TADPCMStream os = new TADPCMStream(pvOutBuffer);
		// Input stream
		TADPCMStream is = new TADPCMStream(pvInBuffer);
		byte EncodedSample;
		byte BitShift;
		// Predicted sample for each channel
		short[] PredictedSamples = new short[MAX_ADPCM_CHANNEL_COUNT];
		// Predicted step index for each channel
		short[] StepIndexes = new short[MAX_ADPCM_CHANNEL_COUNT];
		// Current channel index
		int ChannelIndex;

		// Initialize the StepIndex for each channel
		StepIndexes[0] = StepIndexes[1] = INITIAL_ADPCM_STEP_INDEX;

		// _tprintf(_T("== DCMP Started ==============\n"));

		// The first byte is always zero, the second one contains bit shift
		// (compression level - 1)
		is.ReadByteSample();
		BitShift = is.ReadByteSample().get();
		// _tprintf(_T("DCMP: BitShift = %u\n"), (unsigned int)(unsigned
		// char)BitShift);

		// Next, InitialSample value for each channel follows
		for (int i = 0; i < ChannelCount; i++) {
			// Get the initial sample from the input stream
			short InitialSample;

			// Attempt to read the initial sample
			Optional<Short> InitialSampleOpt = is.ReadWordSample();
			if (!InitialSampleOpt.isPresent())
				return os.LengthProcessed();
			InitialSample = InitialSampleOpt.get();

			// _tprintf(_T("DCMP: Loaded InitialSample[%u]: %04X\n"), i,
			// (unsigned int)(unsigned short)InitialSample);

			// Store the initial sample to our sample array
			PredictedSamples[i] = InitialSample;

			// Also store the loaded sample to the output stream
			if (!os.WriteWordSample(InitialSample))
				return os.LengthProcessed();
		}

		// Get the initial index
		ChannelIndex = ChannelCount - 1;

		// Keep reading as long as there is something in the input buffer
		while (true) {
			Optional<@Unsigned Byte> EncodedSampleOpt = is.ReadByteSample();
			if (!EncodedSampleOpt.isPresent()) {
				break;
			}
			EncodedSample = EncodedSampleOpt.get();
			// _tprintf(_T("DCMP: Loaded Encoded Sample: %02X\n"), (unsigned
			// int)(unsigned char)EncodedSample);

			// If we have two channels, we need to flip the channel index
			ChannelIndex = (ChannelIndex + 1) % ChannelCount;

			if (EncodedSample == 0x80) {
				if (StepIndexes[ChannelIndex] != 0)
					StepIndexes[ChannelIndex]--;

				// _tprintf(_T("DCMP: Writing Decoded Sample: %04lX\n"),
				// (unsigned int)(unsigned
				// short)PredictedSamples[ChannelIndex]);
				if (!os.WriteWordSample(PredictedSamples[ChannelIndex]))
					return os.LengthProcessed();
			} else if (EncodedSample == 0x81) {
				// Modify the step index
				StepIndexes[ChannelIndex] += 8;
				if (StepIndexes[ChannelIndex] > 0x58)
					StepIndexes[ChannelIndex] = 0x58;

				// _tprintf(_T("DCMP: New value of StepIndex: %04lX\n"),
				// (unsigned int)(unsigned short)StepIndexes[ChannelIndex]);

				// Next pass, keep going on the same channel
				ChannelIndex = (ChannelIndex + 1) % ChannelCount;
			} else {
				int StepIndex = StepIndexes[ChannelIndex];
				int StepSize = StepSizeTable[StepIndex];

				// Encode one sample
				PredictedSamples[ChannelIndex] = (short) DecodeSample(PredictedSamples[ChannelIndex], EncodedSample,
						StepSize, StepSize >> BitShift);

				// _tprintf(_T("DCMP: Writing decoded sample: %04X\n"),
				// (unsigned int)(unsigned
				// short)PredictedSamples[ChannelIndex]);

				// Write the decoded sample to the output stream
				if (!os.WriteWordSample(PredictedSamples[ChannelIndex]))
					break;

				// Calculates the step index to use for the next encode
				StepIndexes[ChannelIndex] = GetNextStepIndex(StepIndex, EncodedSample);
				// _tprintf(_T("DCMP: New step index: %04X\n"), (unsigned
				// int)(unsigned short)StepIndexes[ChannelIndex]);
			}
		}

		// _tprintf(_T("DCMP: Total length written: %u\n"), (unsigned
		// int)os.LengthProcessed(pvOutBuffer));
		// _tprintf(_T("== DCMP Ended ================\n"));

		// Return total bytes written since beginning of the output buffer
		return os.LengthProcessed();
	}

}
