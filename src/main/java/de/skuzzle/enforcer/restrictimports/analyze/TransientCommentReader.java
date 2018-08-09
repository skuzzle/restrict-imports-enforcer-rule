package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

/**
 * A filtering Reader which hides all comments in the input from its user. Comments are
 * Java style block or inline comments. When reading an inline comment, the end of line
 * character (either \r or \n) is not considered part of the comment.
 * <p>
 * When reading block comments that span multiple lines, this reader can be set to replace
 * the whole block comment by the same amount of empty lines. All common line endings are
 * supported: CR (Mac), LF (Unix) and CRLF (Windows). However, the empty lines added back
 * will always be LF. When reading a file line by line this allows to keep track of the
 * correct line number.
 *
 * @author Simon Taddiken
 */
class TransientCommentReader extends Reader {

    private boolean eos = false;
    private final boolean trackLineBreaks;
    private final PushbackReader in;
    private final int bufferSize;

    protected TransientCommentReader(Reader in, boolean trackLineBreaks,
            int expectedCommentLines) {
        this.bufferSize = trackLineBreaks
                ? expectedCommentLines
                : 1;

        this.in = new PushbackReader(in, bufferSize);
        this.trackLineBreaks = trackLineBreaks;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        synchronized (lock) {
            if (len <= 0) {
                if (len < 0) {
                    throw new IndexOutOfBoundsException();
                } else if ((off < 0) || (off > cbuf.length)) {
                    throw new IndexOutOfBoundsException();
                }
                return 0;
            }

            int i = 0;
            for (; i < len; ++i) {
                final int next = read();
                if (next == -1) {
                    return i == 0 ? -1 : i;
                } else {
                    cbuf[i + off] = (char) next;
                }
            }
            return i;
        }
    }

    @Override
    public int read() throws IOException {
        if (this.eos) {
            return -1;
        } else {
            return commentRead();
        }
    }

    private int commentRead() throws IOException {
        int next = saveRead();
        switch (next) {
        case '"':
        case '/':
            // possible begin of a comment
            next = saveRead();

            switch (next) {
            case '*':
                return skipBlockComment();
            case '/':
                // begin of an inline comment
                return skipInlineComment();
            default:
                // just a single slash, followed by something else
                saveUnread(next);
                return '/';
            }

        default:
            return next;
        }
    }

    private int skipBlockComment() throws IOException {
        int skippedLines = 0;
        while (!this.eos) {
            int next = saveRead();

            if (next == '\r') {
                // mac (CR) or windows (CRLF) line break
                ++skippedLines;
                next = saveRead();
                if (next == '\n') {
                    continue;
                }
            } else if (next == '\n') {
                ++skippedLines;
                continue;
            }

            if (next == '*') {
                next = saveRead();
                if (next == '/') {
                    // end of block comment
                    if (trackLineBreaks) {
                        if (skippedLines > this.bufferSize) {
                            throw new CommentBufferOverflowException(String.format(
                                    "Encountered %d skipped lines in a block comment but buffer size is %d",
                                    skippedLines, bufferSize));
                        }
                        // push back empty lines
                        for (int i = 0; i < skippedLines; ++i) {
                            saveUnread('\n');
                        }
                    }
                    return read();
                } else {
                    this.saveUnread(next);
                }
            }
        }
        return -1;
    }

    private int skipInlineComment() throws IOException {
        while (!this.eos) {
            final int next = saveRead();

            if (next == '\r' || next == '\n') {
                // end of comment
                return next;
            }
        }
        return -1;
    }

    private int saveRead() throws IOException {
        if (this.eos) {
            return -1;
        }
        final int next = in.read();
        this.eos = next == -1;
        return next;
    }

    private void saveUnread(int c) throws IOException {
        if (c == -1) {
            this.eos = true;
        } else {
            this.in.unread(c);
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
