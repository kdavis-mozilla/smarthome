/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.voice.internal;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.HashSet;
import java.util.Locale;

import ee.ioc.phon.netspeechapi.duplex.WsDuplexRecognitionSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.io.audio.AudioFormat;
import org.eclipse.smarthome.io.audio.AudioSource;
import org.eclipse.smarthome.io.voice.STTException;
import org.eclipse.smarthome.io.voice.STTService;
import org.eclipse.smarthome.io.voice.STTListener;
import org.eclipse.smarthome.io.voice.STTServiceHandle;

/**
 * This is a STT service implementation using Kaldi.
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public class STTServiceKaldi implements STTService {

    private static final Logger logger = LoggerFactory.getLogger(STTServiceKaldi.class);

   /**
    * WebSocket URL to the head node of the Kaldi server cluster
    */
    private static final String kaldiWebSocketURL = "ws://52.37.26.79:8888/client/ws/speech";

    /**
     * Set of supported locales
     */
     private final HashSet<Locale> locales = initLocales();

    /**
     * Set of supported audio formats
     */
     private final HashSet<AudioFormat> audioFormats = initAudioFormats();

    /**
     * {@inheritDoc}
     */
    public Set<Locale> getSupportedLocales() {
        return this.locales;
    }

    /**
     * {@inheritDoc}
     */
    public Set<AudioFormat> getSupportedFormats() {
        return this.audioFormats;
    }

    /**
     * {@inheritDoc}
     */
    public STTServiceHandle recognize(STTListener sttListener, AudioSource audioSource, Locale locale, Set<String> grammars) throws STTException {
        // Validate arguments
        if (null == sttListener) {
            throw new IllegalArgumentException("The passed STTListener is null");
        }
        if (null == audioSource) {
            throw new IllegalArgumentException("The passed AudioSource is null");
        }
        boolean isAudioFormatValid = false;
        AudioFormat audioFormat = audioSource.getFormat();
        for (AudioFormat currentAudioFormat : this.audioFormats) {
            if (currentAudioFormat.isCompatible(audioFormat)) {
                isAudioFormatValid = true;
                break;
            }
        }
        if (!isAudioFormatValid) {
            throw new IllegalArgumentException("The passed AudioSource's AudioFormat is unsupported");
        }
        if (null == audioFormat.getBitRate()) {
            throw new IllegalArgumentException("The passed AudioSource's AudioFormat's bit rate is not set");
        }
        if (!this.locales.contains(locale)) {
            throw new IllegalArgumentException("The passed Locale is unsupported");
        }
        // Note: Currently Kaldi doesn't use grammars. Thus grammars isn't validated

        // Setup WsDuplexRecognitionSession
        WsDuplexRecognitionSession recognitionSession;
        try {
            recognitionSession = new WsDuplexRecognitionSession(kaldiWebSocketURL);
        } catch(IOException e) {
            throw new STTException("Error connected to the server", e);
        } catch(URISyntaxException e) {
            throw new STTException("Invalid WebSocket URL", e);
        }
        // One need not call recognitionSession.setContentType(...) [See http://bit.ly/1TGvQzA]
        recognitionSession.addRecognitionEventListener(new RecognitionEventListenerKaldi(sttListener));

        // Start recognition
        STTServiceKaldiRunnable sttServiceKaldiRunnable = new STTServiceKaldiRunnable(recognitionSession, sttListener, audioSource);
        Thread thread = new Thread(sttServiceKaldiRunnable);
        thread.start();

        // Return STTServiceHandleKaldi
        return new STTServiceHandleKaldi(sttServiceKaldiRunnable);
    }

    /**
     * Initializes this.locales
     *
     * @return The locales of this instance
     */
     private final HashSet<Locale> initLocales() {
         HashSet<Locale> locales = new HashSet<Locale>();
         locales.add(new Locale("en", "US")); // For now we only support American English
         return locales;
     }

    /**
     * Initializes this.audioFormats
     *
     * @return The audio formats of this instance
     */
     private final HashSet<AudioFormat> initAudioFormats() {
         HashSet<AudioFormat> audioFormats = new HashSet<AudioFormat>();
         
          String containers[] = {"NONE", "ASF", "AVI", "DVR-MS", "MKV", "MPEG", "OGG", "QuickTime", "RealMedia", "WAVE"};
          String codecs[] = {"PCM_SIGNED", "RAW", "A52", "ADPCM", "FLAC", "GSM", "A-LAW", "MU-LAW", "MP3", "QDM", "SPEEX", "VORBIS", "NIST", "VOC"};

          for (String container : containers) {
              for (String codec : codecs) {
                  audioFormats.add(new AudioFormat(container, codec, null, null, null, null)); // TODO: Allow only valid combinations
              }
          }

         return audioFormats;
     }
}
