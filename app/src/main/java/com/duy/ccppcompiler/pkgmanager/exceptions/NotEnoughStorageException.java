package com.duy.ccppcompiler.pkgmanager.exceptions;

public class NotEnoughStorageException extends Exception {
    private long needMem;

    public NotEnoughStorageException(int needMem) {
        this.needMem = needMem;
    }

    @Override
    public String getMessage() {
        return "Not enough storage to store file. Need " + needMem + " bytes";
    }

    public long getNeedMem() {
        return needMem;
    }
}
