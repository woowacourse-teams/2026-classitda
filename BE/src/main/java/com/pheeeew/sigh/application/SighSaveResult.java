package com.pheeeew.sigh.application;

import com.pheeeew.sigh.domain.Sigh;

public record SighSaveResult(Sigh sigh, boolean created) {

    public static SighSaveResult of(Sigh sigh, boolean created) {
        return new SighSaveResult(sigh, created);
    }
}
