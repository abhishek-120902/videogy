/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.download;

//interface created outside DownloadsInactive in a different file to avoid cyclic inheritance
public interface OnDownloadWithNewLinkListener {
    void onDownloadWithNewLink(DownloadVideo download);
}
