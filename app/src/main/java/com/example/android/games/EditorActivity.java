
package com.example.android.games;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.games.data.GameContract.GameEntry;

/**
 * Allows user to create a new game or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the game data loader */
    private static final int EXISTING_GAME_LOADER = 0;

    /** Content URI for the existing game (null if it's a new game) */
    private Uri mCurrentGameUri;

    /** EditText field to enter the game's name */
    private EditText mNameEditText;

    /** EditText field to enter the game's brand */
    private EditText mBrandEditText;

    /** EditText field to enter the game's price */
    private EditText mPriceEditText;

    /** EditText field to enter the game's demographic */
    private Spinner mDemographicSpinner;

    /**
     * Demographic of the game. The possible valid values are in the GameContract.java file:
     * {@link GameEntry#DEMOGRAPHIC_FAMILY}, {@link GameEntry#DEMOGRAPHIC_CHILDREN}, or
     * {@link GameEntry#DEMOGRAPHIC_ADULT}.
     */
    private int mDemographic = GameEntry.DEMOGRAPHIC_FAMILY;

    /** Boolean flag that keeps track of whether the game has been edited (true) or not (false) */
    private boolean mGameHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mGameHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mGameHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new game or editing an existing one.
        Intent intent = getIntent();
        mCurrentGameUri = intent.getData();

        // If the intent DOES NOT contain a game content URI, then we know that we are
        // creating a new game.
        if (mCurrentGameUri == null) {
            // This is a new game, so change the app bar to say "Add a Game"
            setTitle(getString(R.string.editor_activity_title_new_game));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a game that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing game, so change app bar to say "Edit Game"
            setTitle(getString(R.string.editor_activity_title_edit_game));

            // Initialize a loader to read the game data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_GAME_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_game_name);
        mBrandEditText = (EditText) findViewById(R.id.edit_game_brand);
        mPriceEditText = (EditText) findViewById(R.id.edit_game_price);
        mDemographicSpinner = (Spinner) findViewById(R.id.spinner_demographic);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mBrandEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mDemographicSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the demographic of the game.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter demographicSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_demographic_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        demographicSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mDemographicSpinner.setAdapter(demographicSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mDemographicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.demographic_children))) {
                        mDemographic = GameEntry.DEMOGRAPHIC_CHILDREN;
                    } else if (selection.equals(getString(R.string.demographic_adult))) {
                        mDemographic = GameEntry.DEMOGRAPHIC_ADULT;
                    } else {
                        mDemographic = GameEntry.DEMOGRAPHIC_FAMILY;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDemographic = GameEntry.DEMOGRAPHIC_FAMILY;
            }
        });
    }

    /**
     * Get user input from editor and save game into database.
     */
    private void saveGame() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String brandString = mBrandEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        // Check if this is supposed to be a new game
        // and check if all the fields in the editor are blank
        if (mCurrentGameUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(brandString) &&
                TextUtils.isEmpty(priceString) && mDemographic == GameEntry.DEMOGRAPHIC_FAMILY) {
            // Since no fields were modified, we can return early without creating a new game.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and game attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(GameEntry.COLUMN_GAME_NAME, nameString);
        values.put(GameEntry.COLUMN_GAME_BRAND, brandString);
        values.put(GameEntry.COLUMN_GAME_DEMOGRAPHIC, mDemographic);
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(GameEntry.COLUMN_GAME_PRICE, price);

        // Determine if this is a new or existing game by checking if mCurrentGameUri is null or not
        if (mCurrentGameUri == null) {
            // This is a NEW game, so insert a new game into the provider,
            // returning the content URI for the new game.
            Uri newUri = getContentResolver().insert(GameEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_game_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_game_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING game, so update the game with content URI: mCurrentGameUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentGameUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentGameUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_game_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_game_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new game, hide the "Delete" menu item.
        if (mCurrentGameUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save game to database
                saveGame();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the game hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mGameHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the game hasn't changed, continue with handling back button press
        if (!mGameHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all game attributes, define a projection that contains
        // all columns from the game table
        String[] projection = {
                GameEntry._ID,
                GameEntry.COLUMN_GAME_NAME,
                GameEntry.COLUMN_GAME_BRAND,
                GameEntry.COLUMN_GAME_DEMOGRAPHIC,
                GameEntry.COLUMN_GAME_PRICE };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentGameUri,         // Query the content URI for the current game
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of game attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_NAME);
            int brandColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_BRAND);
            int demographicColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_DEMOGRAPHIC);
            int priceColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_PRICE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String brand = cursor.getString(brandColumnIndex);
            int demographic = cursor.getInt(demographicColumnIndex);
            int price = cursor.getInt(priceColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mBrandEditText.setText(brand);
            mPriceEditText.setText(Integer.toString(price));

            // Demographic is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (demographic) {
                case GameEntry.DEMOGRAPHIC_CHILDREN:
                    mDemographicSpinner.setSelection(1);
                    break;
                case GameEntry.DEMOGRAPHIC_ADULT:
                    mDemographicSpinner.setSelection(2);
                    break;
                default:
                    mDemographicSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mBrandEditText.setText("");
        mPriceEditText.setText("");
        mDemographicSpinner.setSelection(0); // Select "Unknown" demographic
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the game.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this game.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the game.
                deleteGame();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the game.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the game in the database.
     */
    private void deleteGame() {
        // Only perform the delete if this is an existing game.
        if (mCurrentGameUri != null) {
            // Call the ContentResolver to delete the game at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentGameUri
            // content URI already identifies the game that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentGameUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_game_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_game_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}