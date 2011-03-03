package com.danga.squeezer.itemlists;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.danga.squeezer.R;
import com.danga.squeezer.SqueezerBaseListActivity;
import com.danga.squeezer.SqueezerItem;
import com.danga.squeezer.SqueezerItemView;
import com.danga.squeezer.itemlists.GenreSpinner.GenreSpinnerCallback;
import com.danga.squeezer.model.SqueezerAlbum;
import com.danga.squeezer.model.SqueezerArtist;
import com.danga.squeezer.model.SqueezerGenre;

public class SqueezerArtistListActivity extends SqueezerBaseListActivity<SqueezerArtist> implements GenreSpinnerCallback{
	private static final int DIALOG_FILTER = 0;

	private String searchString = null;
	private SqueezerAlbum album;
	private SqueezerGenre genre;

	private GenreSpinner genreSpinner;

	public SqueezerGenre getGenre() {
		return genre;
	}

	public SqueezerItemView<SqueezerArtist> createItemView() {
		return new SqueezerArtistView(this);
	}

	@Override
	public void prepareActivity(Bundle extras) {
		if (extras != null)
			for (String key : extras.keySet()) {
				if (SqueezerAlbum.class.getName().equals(key)) {
					album = extras.getParcelable(key);
				} else if (SqueezerGenre.class.getName().equals(key)) {
					genre = extras.getParcelable(key);
				} else
					Log.e(getTag(), "Unexpected extra value: " + key + "("
							+ extras.get(key).getClass().getName() + ")");
			}
	}

	public void registerCallback() throws RemoteException {
		getService().registerArtistListCallback(artistsListCallback);
		if (genreSpinner != null) genreSpinner.registerCallback();
	}

	public void unregisterCallback() throws RemoteException {
		getService().unregisterArtistListCallback(artistsListCallback);
		if (genreSpinner != null) genreSpinner.unregisterCallback();
	}

	public void orderItems(int start) throws RemoteException {
		getService().artists(start, searchString, album, genre);
	}

	public void onItemSelected(int index, SqueezerArtist item) throws RemoteException {
		SqueezerAlbumListActivity.show(this, item);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_filter:
			showDialog(DIALOG_FILTER);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public boolean onSearchRequested() {
		showDialog(DIALOG_FILTER);
		return false;
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch (id) {
		case DIALOG_FILTER:

			View filterForm = getLayoutInflater().inflate(R.layout.filter_dialog, null);
			builder.setTitle(R.string.menu_item_filter);
			builder.setView(filterForm);

	        final EditText editText = (EditText) filterForm.findViewById(R.id.search_string);
	        editText.setHint(getString(R.string.filter_text_hint, getItemListAdapter().getQuantityString(2)));
			filterForm.findViewById(R.id.year_view).setVisibility(View.GONE);
			final Spinner genreSpinnerView = (Spinner) filterForm.findViewById(R.id.genre_spinner);
	        ImageButton filterButton = (ImageButton) filterForm.findViewById(R.id.button_filter);
	        ImageButton cancelButton = (ImageButton) filterForm.findViewById(R.id.button_cancel);
	        genreSpinner = new GenreSpinner(this, this, genreSpinnerView);
	        
	        if (album != null) { 
	        	((EditText)filterForm.findViewById(R.id.album)).setText(album.getName());
	        	(filterForm.findViewById(R.id.album_view)).setVisibility(View.VISIBLE);
	        }
	        
	        editText.setOnKeyListener(new OnKeyListener() {
	            public boolean onKey(View v, int keyCode, KeyEvent event) {
	                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
	                	searchString = editText.getText().toString();
						genre = (SqueezerGenre) genreSpinnerView.getSelectedItem();
						orderItems();
						dismissDialog(DIALOG_FILTER);
	                  return true;
	                }
	                return false;
	            }
	        });
	        
	        filterButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
                	searchString = editText.getText().toString();
					genre = (SqueezerGenre) genreSpinnerView.getSelectedItem();
					orderItems();
					dismissDialog(DIALOG_FILTER);
				}
			});
	        cancelButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismissDialog(DIALOG_FILTER);
				}
			});
	        
			return builder.create();
        }
        return null;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	menu.findItem(R.id.menu_item_sort).setVisible(false);
    	return true;
    }

    
	public static void show(Context context, SqueezerItem... items) {
        final Intent intent = new Intent(context, SqueezerArtistListActivity.class);
        for (SqueezerItem item: items)
        	intent.putExtra(item.getClass().getName(), item);
        context.startActivity(intent);
    }

    private IServiceArtistListCallback artistsListCallback = new IServiceArtistListCallback.Stub() {
		public void onArtistsReceived(int count, int max, int start, List<SqueezerArtist> items) throws RemoteException {
			onItemsReceived(count, max, start, items);
		}
    };

}
