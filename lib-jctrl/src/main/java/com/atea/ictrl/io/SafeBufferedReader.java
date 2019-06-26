/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.atea.ictrl.io;

import java.io.*;

public class SafeBufferedReader extends BufferedReader {

    public SafeBufferedReader(Reader in) {
        this(in, 1024);
    }

    public SafeBufferedReader(Reader in, int bufferSize) {
        super(in, bufferSize);
    }

    private boolean lookingForLineFeed = false;

    @Override
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder("");
        while (true) {
            int c = this.read();
            if (c == -1) { // end of stream
                if (sb.length() == 0) return null;
                return sb.toString();
            } else if (c == '\n') {
                if (lookingForLineFeed) {
                    lookingForLineFeed = false;
                    continue;
                } else {
                    return sb.toString();
                }
            } else if (c == '\r') {
                lookingForLineFeed = true;
                return sb.toString();
            } else {
                lookingForLineFeed = false;
                sb.append((char) c);
            }
        }
    }
}
