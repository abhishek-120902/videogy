/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.download;

import java.io.Serializable;

public class DownloadVideo implements Serializable {
    public String size;
    public String type;
    public String link;
    public String name;
    public String page;
    public String website;
    public boolean chunked;
}
