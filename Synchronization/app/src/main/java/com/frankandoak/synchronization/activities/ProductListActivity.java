package com.frankandoak.synchronization.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.frankandoak.synchronization.R;
import com.frankandoak.synchronization.fragments.ProductListFragment;
import com.frankandoak.synchronization.models.RemoteCategory;
import com.frankandoak.synchronization.services.SyncCategoryProductsService;


public class ProductListActivity extends ActionBarActivity {

    public static final class EXTRAS {
        public static String IN_CATEGORY_ID = "inCategoryId";
    }

    private RemoteCategory mCategory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        mCategory = getIntent().getParcelableExtra(EXTRAS.IN_CATEGORY_ID);

        ProductListFragment fragment = ProductListFragment.newInstance(mCategory);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_product_list_container, fragment, ProductListFragment.class.getSimpleName())
                .commit();

        setTitle(mCategory.getName());
    }


    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, SyncCategoryProductsService.class);
        intent.setAction(Intent.ACTION_SYNC);
        intent.putExtra(SyncCategoryProductsService.EXTRAS.IN_CATEGORY_ID, mCategory.getCategoryId());
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_product_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
