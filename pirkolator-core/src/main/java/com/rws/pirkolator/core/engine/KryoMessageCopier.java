package com.rws.pirkolator.core.engine;

import com.rws.pirkolator.core.utility.serial.KryoUtility;
import com.rws.pirkolator.model.Message;




public class KryoMessageCopier implements IMessageCopier {

    @Override
    public Message copy (final Message message) {
        
        return KryoUtility.copyMessage (message);
    }

}
