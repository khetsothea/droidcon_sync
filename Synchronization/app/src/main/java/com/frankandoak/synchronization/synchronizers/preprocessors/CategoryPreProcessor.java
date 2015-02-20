package com.frankandoak.synchronization.synchronizers.preprocessors;


import com.frankandoak.synchronization.models.RemoteCategory;
import com.frankandoak.synchronization.models.RemoteProduct;

import java.util.List;

/**
 * Created by Michael on 2014-03-17.
 */
public class CategoryPreProcessor extends BasePreProcessor<RemoteCategory> {

    @Override
    public void preProcessRemoteRecords(List<RemoteCategory> records) {

        int i = 0;
        for( RemoteCategory catProduct : records ) {
            if( catProduct != null ) {
                catProduct.setPosition(i);

                ++i;
            }
        }
    }
}
