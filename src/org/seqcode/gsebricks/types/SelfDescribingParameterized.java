package org.seqcode.gsebricks.types;

import java.util.Map;


public interface SelfDescribingParameterized {

    public void init(Map<String,Object> params);
    public EchoType[] getParameterClasses();
    public String[] getParameterNames();
}
