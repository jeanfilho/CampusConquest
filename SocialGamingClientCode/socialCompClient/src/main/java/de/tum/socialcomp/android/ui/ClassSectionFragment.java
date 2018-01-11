package de.tum.socialcomp.android.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import de.tum.socialcomp.android.MainActivity;
import de.tum.socialcomp.android.R;
import de.tum.socialcomp.android.webservices.util.HttpPoster;

/**
 * Represents the class selection fragment
 * Class selection directly specifies which abilities and character stats a player will have in the game
 *
 * Created by Michael on 24.06.2015.
 */
public class ClassSectionFragment extends Fragment {
    View rootView;

    private Bitmap[] class_bitmaps = new Bitmap[6];

    /*chosen IDs for left (first value) and right (second value) side
    * -1 if none
    * */
    private int[] chosenIDs = {-1,-1};

    private int size_of_class_bitmaps = 110;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.class_fragment, container, false);

        class_bitmaps[0] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.class_assault), size_of_class_bitmaps, size_of_class_bitmaps, true);
        class_bitmaps[1] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.class_conquerer), size_of_class_bitmaps, size_of_class_bitmaps, true);
        class_bitmaps[2] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.class_medic), size_of_class_bitmaps, size_of_class_bitmaps, true);
        class_bitmaps[3] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.class_spy), size_of_class_bitmaps, size_of_class_bitmaps, true);
        class_bitmaps[4] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.class_saboteur), size_of_class_bitmaps, size_of_class_bitmaps, true);
        // default class
        class_bitmaps[5] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.class_default), size_of_class_bitmaps, size_of_class_bitmaps, true);

        //should only be available if two classes were chosen
        rootView.findViewById(R.id.confirm_button).setEnabled(false);

        // if the user taps on one of a class images, a short info text should be shown
        rootView.findViewById(R.id.image_assault).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClassSectionFragment.showClassDescription(0);
                    }
                });
        rootView.findViewById(R.id.image_conquerer).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClassSectionFragment.showClassDescription(1);
                    }
                });
        rootView.findViewById(R.id.image_medic).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClassSectionFragment.showClassDescription(2);
                    }
                });
        rootView.findViewById(R.id.image_spy).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClassSectionFragment.showClassDescription(3);
                    }
                });
        rootView.findViewById(R.id.image_saboteur).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClassSectionFragment.showClassDescription(4);
                    }
                });

        //Image-Button Left
        rootView.findViewById(R.id.imageButtonLeft).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clearImageButton(0);
                    }
                });

        //Image-Button Right
        rootView.findViewById(R.id.imageButtonRight).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clearImageButton(1);
                    }
                });


        //Asault id = 0
        rootView.findViewById(R.id.button0).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setImage(0);
                    }
                });

        //Conquerer id = 1
        rootView.findViewById(R.id.button1).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setImage(1);
                    }
                });

        //Medic id = 3
        rootView.findViewById(R.id.button2).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setImage(2);
                    }
                });

        //Spy id = 3
        rootView.findViewById(R.id.button3).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setImage(3);
                    }
                });

        //Saboteur id = 4
        rootView.findViewById(R.id.button4).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setImage(4);
                    }
                });

        rootView.findViewById(R.id.confirm_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        confirmChoice();
                    }
                });
        MainActivity.menu.clear();
        MainActivity.getInstance().getMenuInflater().inflate(R.menu.misc, MainActivity.menu);
        return rootView;
    }

    @Override
    public void onResume() {
        reset();
        super.onResume();
    }

    private void reset(){
        // reset ImageButtons
        ((ImageButton) rootView.findViewById(R.id.imageButtonLeft)).setImageBitmap(class_bitmaps[5]);
        ((ImageButton) rootView.findViewById(R.id.imageButtonRight)).setImageBitmap(class_bitmaps[5]);
        // reset choseIDs
        chosenIDs[0] = -1;
        chosenIDs[1] = -1;
    }

    /**
     * set the image of the specified class on a free imageButton if possible
     *
     * @param id
     */
    private void setImage(int id){
        if(chosenIDs[0] == -1){
            ((ImageButton) rootView.findViewById(R.id.imageButtonLeft)).setImageBitmap(class_bitmaps[id]);
            chosenIDs[0] = id;
            setButtonEnabled(id, false);
        }else if(chosenIDs[1] == -1){
            ((ImageButton) rootView.findViewById(R.id.imageButtonRight)).setImageBitmap(class_bitmaps[id]);
            chosenIDs[1] = id;
            setButtonEnabled(id, false);
        }
        // If two classes are chosen, show corresponding name:
        if(chosenIDs[0] != -1 && chosenIDs[1] != -1){
            ((TextView)rootView.findViewById(R.id.class_name)).setText(MainActivity.superClasses[chosenIDs[0]][chosenIDs[1]]);
            rootView.findViewById(R.id.confirm_button).setEnabled(true);
        }else{
            ((TextView)rootView.findViewById(R.id.class_name)).setText("Choose a combination");
        }
    }

    /**
     *
     * @param id
     * @param enabled
     */
    private void setButtonEnabled(int id, boolean enabled){
        switch(id){
            case 0:rootView.findViewById(R.id.button0).setEnabled(enabled);
                break;
            case 1:rootView.findViewById(R.id.button1).setEnabled(enabled);
                break;
            case 2:rootView.findViewById(R.id.button2).setEnabled(enabled);
                break;
            case 3:rootView.findViewById(R.id.button3).setEnabled(enabled);
                break;
            case 4:rootView.findViewById(R.id.button4).setEnabled(enabled);
                break;
            default:
                break;
        }
    }

    private void confirmChoice() {
        if(chosenIDs[0] != -1 && chosenIDs[1] != -1) {
            new HttpPoster().execute(new String[]{
                    "users", MainActivity.facebookID,
                    chosenIDs[0] + "", chosenIDs[1] + "", "setClasses"});
        }
        MainActivity.chosenClassIDs[0] = chosenIDs[0];
        MainActivity.chosenClassIDs[1] = chosenIDs[1];
        MainActivity.getInstance().setActionBarTitle("Live Map");
        MainActivity.getInstance().switchToFragment(MainActivity.getInstance().mapsectionfrag);
    }

    /**
     *
     * @param classID
     */
    public static void showClassDescription(int classID){
        MainActivity.BaseClass baseClass = MainActivity.baseClasses[classID];
        String classDescription =
                baseClass.name + "\n" + baseClass.description;
        Toast.makeText(MainActivity.getInstance().getApplicationContext(), classDescription,
                Toast.LENGTH_LONG).show();
    }

    /**
     *
     * @param index
     */
    private void clearImageButton(int index){
        int rID = index == 0 ? R.id.imageButtonLeft : R.id.imageButtonRight;
        ((ImageButton) rootView.findViewById(rID)).setImageBitmap(class_bitmaps[5]);
        setButtonEnabled(chosenIDs[index], true);
        rootView.findViewById(R.id.confirm_button).setEnabled(false);
        ((TextView)rootView.findViewById(R.id.class_name)).setText("Choose a combination");
        chosenIDs[index] = -1;
    }
}
