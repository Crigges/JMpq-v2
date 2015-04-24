package de.peeeq.jmpq.adpcm;

import java.nio.ByteBuffer;
import java.util.Optional;

//-----------------------------------------------------------------------------
// Helper class for writing output ADPCM data

class TADPCMStream {

	private ByteBuffer pbBuffer;

	TADPCMStream(ByteBuffer buffer) {
		pbBuffer = buffer;
	}

	Optional<@Unsigned Byte> ReadByteSample() {
		// Check if there is enough space in the buffer
		if (!pbBuffer.hasRemaining()) {
			return Optional.empty();
		}

		byte b = pbBuffer.get();
		return Optional.of(b);
	}

	boolean WriteByteSample(@Unsigned byte byteSample) {
		// Check if there is enough space in the buffer
		if (!pbBuffer.hasRemaining()) {
			return false;
		}

		pbBuffer.put(byteSample);
		return true;
	}

	Optional<Short> ReadWordSample() {
		// Check if we have enough space in the output buffer
		if (pbBuffer.remaining() < 2) {
			return Optional.empty();
		}

		// Write the sample
		short s = pbBuffer.getShort();
		return Optional.of(s);
	}

	boolean WriteWordSample(short OneSample) {
		// Check if there is enough space in the buffer
		if (pbBuffer.remaining() < 2) {
			return false;
		}

		pbBuffer.putShort(OneSample);
		return true;
	}
	
	int LengthProcessed() {
		throw new Error("TODO");
	}
	
}