package com.elduin.claude_friend.client;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.elduin.claude_friend.ClaudeFriend;

/**
 * Claude's voice: the Mac's own speech, in its deepest built-in voice.
 * Does nothing on Windows or Linux.
 */
public final class Voice {

	private static final String MAC_VOICE = "Ralph";
	private static final int MAX_CHARS = 300;

	private static Process speaking;

	private Voice() {
	}

	public static synchronized void say(String text) {
		if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac")) {
			return;
		}
		// a new answer cuts off the old one instead of talking over it
		if (speaking != null && speaking.isAlive()) {
			speaking.destroy();
		}
		String line = text.length() > MAX_CHARS ? text.substring(0, MAX_CHARS) : text;
		try {
			speaking = new ProcessBuilder("say", "-v", MAC_VOICE)
					.redirectErrorStream(true)
					.redirectOutput(ProcessBuilder.Redirect.DISCARD)
					.start();
			// The words go in on stdin, never as an argument, so text from a server
			// can't be read as one of say's options (like -o, which writes a file).
			try (OutputStream in = speaking.getOutputStream()) {
				in.write(line.getBytes(StandardCharsets.UTF_8));
			}
		} catch (IOException e) {
			ClaudeFriend.LOGGER.warn("Claude couldn't speak", e);
		}
	}
}
