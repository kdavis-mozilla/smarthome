/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test SpeechRecognitionErrorEvent event
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class SpeechRecognitionErrorEventTest {
    /**
     * Test SpeechRecognitionErrorEvent(String) constructor
     */
    @Test
    public void testConstructor() {
        SpeechRecognitionErrorEvent sRE = new SpeechRecognitionErrorEvent("Message");
        Assert.assertNotNull("SpeechRecognitionErrorEvent(String) constructor failed", sRE);
    }

    /**
     * Test SpeechRecognitionErrorEvent.getMessage() method
     */
    @Test
    public void getMessageTest() {
        SpeechRecognitionErrorEvent sRE = new SpeechRecognitionErrorEvent("Message");
        Assert.assertEquals("SpeechRecognitionErrorEvent.getMessage() method failed", "Message", sRE.getMessage());
    }
}
