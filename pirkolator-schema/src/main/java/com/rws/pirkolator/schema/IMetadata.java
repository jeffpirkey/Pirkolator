package com.rws.pirkolator.schema;

import java.io.Serializable;
import java.util.Map;


public interface IMetadata extends Serializable {

    Map<String, String> getMap ();
}
