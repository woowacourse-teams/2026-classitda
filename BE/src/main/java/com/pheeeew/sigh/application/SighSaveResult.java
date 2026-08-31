package com.pheeeew.sigh.application;

public record SighSaveResult(SighMapItem sigh, boolean created) {

    public static SighSaveResult of(SighMapItem sigh, boolean created) {
        return new SighSaveResult(sigh, created);
    }
}
