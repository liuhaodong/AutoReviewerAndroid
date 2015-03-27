package edu.cmu.lti.autoreviewer.helper;

import com.interaxon.libmuse.Muse;

/**
 * Created by haodongl on 3/26/15.
 */
public class MuseSingle {
    private static Muse singleMuse ;

    MuseSingle(){

    }

    MuseSingle(Muse muse){
        this.singleMuse = muse;
    }

    public static void setMuse(Muse muse){
        MuseSingle.singleMuse = muse;
    }

    public static Muse getMuse(){
        return MuseSingle.singleMuse;
    }

}
