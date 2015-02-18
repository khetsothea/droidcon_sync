package com.frankandoak.synchronization.synchronizers;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.RemoteException;


import com.frankandoak.synchronization.SYNApplication;
import com.frankandoak.synchronization.database.ProductTable;
import com.frankandoak.synchronization.database.ProductTable;
import com.frankandoak.synchronization.database.SYNDatabaseHelper;
import com.frankandoak.synchronization.models.RemoteProduct;
import com.frankandoak.synchronization.providers.SYNContentProvider;
import com.frankandoak.synchronization.utilities.SyncUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Michael on 2014-03-17.
 */
public class ProductSynchronizer extends BaseSynchronizer<RemoteProduct>{

    private static final String TAG = ProductSynchronizer.class.getSimpleName();

    private boolean mAllowDeletion;

    public ProductSynchronizer(Context context, boolean allowDeletion) {

        super(context);

        mAllowDeletion = allowDeletion;
    }

    @Override
    protected void performSynchronizationOperations(Context context, List<RemoteProduct> inserts, List<RemoteProduct> updates, List<Long> deletions) {

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        if( inserts.size() > 0 )
        {
            doBulkInsertOptimised(inserts);
        }

        for (RemoteProduct w : updates) {
            SyncUtil.Selection selection = SyncUtil.buildSelection(w.getIdentifiers());
            ContentProviderOperation op = ContentProviderOperation
                    .newUpdate(SYNContentProvider.URIS.PRODUCTS_URI)
                    .withSelection(selection.getQuery(), selection.getValues())
                    .withValues(getContentValuesForRemoteEntity(w)).build();

            operations.add(op);
        }

        if( mAllowDeletion ) {
            for (Long id : deletions) {
                ContentProviderOperation op = ContentProviderOperation
                        .newDelete(SYNContentProvider.URIS.PRODUCTS_URI)
                        .withSelection(ProductTable._ID + " = ?", new String[] { String.valueOf(id) })
                        .build();

                operations.add(op);
            }
        }


        try {
            if( inserts.size() > 0 || operations.size() > 0 )
            {
                context.getContentResolver().applyBatch(SYNContentProvider.AUTHORITY, operations);
                context.getContentResolver().notifyChange(SYNContentProvider.URIS.PRODUCTS_URI, null);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }


    private int doBulkInsertOptimised(List<RemoteProduct> inserts) {

        Context context = SYNApplication.getContext();
        SYNDatabaseHelper helper = SYNDatabaseHelper.getInstance(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        DatabaseUtils.InsertHelper inserter = new DatabaseUtils.InsertHelper(db, ProductTable.TABLE_NAME);
        RemoteProduct product;

        db.beginTransaction();
        int numInserted = 0;
        try {
            int len = inserts.size();
            for (int i = 0; i < len; i++) {

                product = inserts.get(i);

                inserter.prepareForInsert();
                inserter.bind(inserter.getColumnIndex(ProductTable.PRODUCT_ID), product.getProductId());
                inserter.bind(inserter.getColumnIndex(ProductTable.SKU), product.getSku());
                inserter.bind(inserter.getColumnIndex(ProductTable.NAME), product.getName());
                inserter.bind(inserter.getColumnIndex(ProductTable.IMAGE_URL), product.getImageUrl());
                inserter.bind(inserter.getColumnIndex(ProductTable.FAVORITE_COUNT), product.getFavoriteCount());
                inserter.bind(inserter.getColumnIndex(ProductTable.PRICE), product.getPrice());
                inserter.execute();
            }
            numInserted = len;
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            inserter.close();
        }
        return numInserted;
    }
}