package ma.nttdata.externals.module.interview.service.impl;

import lombok.RequiredArgsConstructor;
import ma.nttdata.externals.commons.config.TextToSpeechPropertiesConfig;
import ma.nttdata.externals.module.interview.service.TextToSpeechServ;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechModel;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechOptions;
import org.springframework.ai.elevenlabs.api.ElevenLabsApi;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TextToSpeechServImpl implements TextToSpeechServ {

    private final TextToSpeechPropertiesConfig textToSpeechPropertiesConfig;
    private final ElevenLabsApi elevenLabsApi;


    @Override
    public byte[] speak(String text) {
        ElevenLabsTextToSpeechModel textToSpeechModel = ElevenLabsTextToSpeechModel.builder()
                .elevenLabsApi(elevenLabsApi)
                .defaultOptions(ElevenLabsTextToSpeechOptions.builder()
                        .model(textToSpeechPropertiesConfig.getModelId())
                        .voiceId(textToSpeechPropertiesConfig.getVoiceId())
                        .outputFormat("mp3_44100_128")
                        .build())
                .build();

        var voiceSettings = new ElevenLabsApi.SpeechRequest.VoiceSettings(
                textToSpeechPropertiesConfig.getStability(),
                textToSpeechPropertiesConfig.getSimilarityBoost(),
                textToSpeechPropertiesConfig.getStyle(),
                textToSpeechPropertiesConfig.isUseSpeakerBoost(),
                textToSpeechPropertiesConfig.getSpeed()
        );

        var options = ElevenLabsTextToSpeechOptions.builder()
                .model(textToSpeechPropertiesConfig.getModelId())
                .voiceId(textToSpeechPropertiesConfig.getVoiceId())
                .voiceSettings(voiceSettings)
                .outputFormat("mp3_44100_128")
                .build();

        try {
            var prompt = new TextToSpeechPrompt(text, options);
            return textToSpeechModel.call(prompt).getResult().getOutput();
        } catch (Exception e) {
            return new byte[0]; // Return empty audio to fallback to silent mode on frontend
        }
    }
}
