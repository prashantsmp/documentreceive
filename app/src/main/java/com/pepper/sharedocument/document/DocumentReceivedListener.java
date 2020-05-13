package com.pepper.sharedocument.document;

import java.io.File;

/**
 * Created by prasanth.mathavan on 15,April,2020
 */
public interface DocumentReceivedListener {
    void onReceived(File receivedFile);
    void onReceiving(File receivingFile);
}
