package com.pheeeew.sigh.infra;

import com.pheeeew.sigh.application.SighNicknameGenerator;
import koreannickname.KoreanNicknameGenerator;
import org.springframework.stereotype.Component;

@Component
public class KoreanSighNicknameGenerator implements SighNicknameGenerator {

    private final KoreanNicknameGenerator generator = KoreanNicknameGenerator.create();

    @Override
    public String generate() {
        return generator.generate();
    }
}
