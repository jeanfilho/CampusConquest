package de.tum.socialcomp.android.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Map;

import de.tum.socialcomp.android.MainActivity;
import de.tum.socialcomp.android.R;
import de.tum.socialcomp.android.webservices.util.HttpGetter;

/**
 * This class represents the character info fragment
 * It shows the player personal character stats as well as HP, lvl, exp etc, and gives info on what the chose abilities do
 * Created by Michael on 28.06.2015.
 */
public class CharacterSectionFragment extends Fragment{
    View rootView;

    private Bitmap faculty_image;

    private Bitmap class1_image;
    private Bitmap class2_image;
    private int sizeOfClassBitmaps = 40;
    private int sizeofFacultyBitmap = 80;

    /*attribute values in the following order:
    * health, strength, intelligence, dominance, sight*/
    private int[] attributeValues = new int[5];

    private int hp;
    private int maxhp;
    private int expToNext;
    private int exp;
    private int level;

    private boolean strengthBuffed;
    private boolean sightBuffed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.character_fragment, container, false);

        int class1_id = -1;
        int class2_id = -1;

        // set player name
        ((TextView)rootView.findViewById(R.id.text_name)).setText(MainActivity.name);

        /*get character values from Server*/
        try {
            HttpGetter request = new HttpGetter();
            request.execute(new String[]{"users", MainActivity.facebookID, "getAllData"});
            String requestResult = request.get();
            if (!requestResult.isEmpty() && !requestResult.equals("{ }")) {
                JSONObject jsonStats = new JSONObject(requestResult);
                // get attributes
                JSONObject jsonAttributes = jsonStats.getJSONObject("Attributes");
                attributeValues[0] = jsonAttributes.getInt("life");
                attributeValues[1] = jsonAttributes.getInt("strength");
                attributeValues[2] = jsonAttributes.getInt("intelligence");
                attributeValues[3] = jsonAttributes.getInt("dominance");
                attributeValues[4] = jsonAttributes.getInt("sight");

                this.strengthBuffed = jsonAttributes.getBoolean("strengthBuffed");
                this.sightBuffed    = jsonAttributes.getBoolean("sightBuffed");

                // get classes
                JSONObject jsonClasses = jsonStats.getJSONObject("Class");
                class1_id = jsonClasses.getInt("class1");
                class2_id = jsonClasses.getInt("class2");
                // get status
                JSONObject jsonStatus = jsonStats.getJSONObject("Status");
                this.maxhp     = jsonStatus.getInt("maxhp");
                this.hp        = jsonStatus.getInt("hp");
                this.exp       = jsonStatus.getInt("ExP");
                this.expToNext = jsonStatus.getInt("ExPToNext");
                this.level     = jsonStatus.getInt("level");
            }
        }catch(Exception e){
            Log.e("Attributes Exception: ", e.getMessage());
        }

        // Find class image-IDs
        int class1_image_id, class2_image_id;
        switch(class1_id){
            case 0: class1_image_id = R.drawable.class_assault;
                break;
            case 1: class1_image_id = R.drawable.class_conquerer;
                break;
            case 2: class1_image_id = R.drawable.class_medic;
                break;
            case 3: class1_image_id = R.drawable.class_spy;
                break;
            case 4: class1_image_id = R.drawable.class_saboteur;
                break;
            default: class1_image_id = R.drawable.class_default;
        }
        switch(class2_id){
            case 0: class2_image_id = R.drawable.class_assault;
                break;
            case 1: class2_image_id = R.drawable.class_conquerer;
                break;
            case 2: class2_image_id = R.drawable.class_medic;
                break;
            case 3: class2_image_id = R.drawable.class_spy;
                break;
            case 4: class2_image_id = R.drawable.class_saboteur;
                break;
            default: class2_image_id = R.drawable.class_default;
        }
        // Find faculty image-ID
        int faculty_image_id;
        switch(MainActivity.facultyID){
            case 0: faculty_image_id = R.drawable.in_player;
                break;
            case 1: faculty_image_id = R.drawable.ch_player;
                break;
            case 2: faculty_image_id = R.drawable.mw_player;
                break;
            case 3: faculty_image_id = R.drawable.ph_player;
                break;
            case 4: faculty_image_id = R.drawable.ma_player;
                break;
            default: faculty_image_id = R.drawable.feggit;
        }
         /*Initialize Bitmaps*/
        faculty_image = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), faculty_image_id), sizeofFacultyBitmap,sizeofFacultyBitmap, true);
        class1_image = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), class1_image_id), sizeOfClassBitmaps, sizeOfClassBitmaps, true);
        class2_image = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), class2_image_id), sizeOfClassBitmaps, sizeOfClassBitmaps, true);

        /*set images*/
        ((ImageView)rootView.findViewById(R.id.faculty_imageView)).setImageBitmap(faculty_image);
        ((ImageView)rootView.findViewById(R.id.class1_imageView)).setImageBitmap(class1_image);
        ((ImageView)rootView.findViewById(R.id.class2_imageView)).setImageBitmap(class2_image);
        // onClick show a description of the class
        rootView.findViewById(R.id.class1_imageView).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClassSectionFragment.showClassDescription(MainActivity.chosenClassIDs[0]);
                    }
                });
        rootView.findViewById(R.id.class2_imageView).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClassSectionFragment.showClassDescription(MainActivity.chosenClassIDs[1]);
                    }
                });

        ((ImageView)rootView.findViewById(R.id.ability_1)).setImageBitmap(class1_image);
        ((ImageView)rootView.findViewById(R.id.ability_2)).setImageBitmap(class2_image);
        // onClick show a description of the ability
        rootView.findViewById(R.id.ability_1).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAbilityDescription(1);
                    }
                });
        rootView.findViewById(R.id.ability_2).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showAbilityDescription(2);
                    }
                });

        /*set superclass*/
        String superclass;
        if(class1_id != class2_id && class1_id >= 0 && class1_id < 5 &&
                class2_id >= 0 && class2_id < 5){
            superclass = MainActivity.superClasses[class1_id][class2_id];
        }else{
            superclass = "Error";
        }
        ((TextView)rootView.findViewById(R.id.text_superclass)).setText(superclass);

        /*set attribute values*/
        ((TextView)rootView.findViewById(R.id.text_att_health))      .setText("  " + attributeValues[0]);
        ((TextView)rootView.findViewById(R.id.text_att_strength))    .setText("  " + attributeValues[1]);
        //if strength is buffed -> highlight
        if(this.strengthBuffed){
            ((TextView)rootView.findViewById(R.id.text_att_strength)).setTextColor(Color.BLUE);
        }else{
            ((TextView)rootView.findViewById(R.id.text_att_strength)).setTextColor(Color.BLACK);
        }
        ((TextView)rootView.findViewById(R.id.text_att_intelligence)).setText("  " + attributeValues[2]);
        ((TextView)rootView.findViewById(R.id.text_att_dominance))   .setText("  " + attributeValues[3]);
        ((TextView)rootView.findViewById(R.id.text_att_sight))       .setText("  " + attributeValues[4]);
        // if sight is buffed -> highlight
        if(this.sightBuffed){
            ((TextView)rootView.findViewById(R.id.text_att_sight)).setTextColor(Color.BLUE);
        }else{
            ((TextView)rootView.findViewById(R.id.text_att_sight)).setTextColor(Color.BLACK);
        }

        MainActivity.menu.clear();
        MainActivity.getInstance().getMenuInflater().inflate(R.menu.misc, MainActivity.menu);

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();

        // Calling this in onStart because in onCreteView the bars often werent updated
        /*set status bars*/
        ((TextView)rootView.findViewById(R.id.hp_progressText)).setText(this.hp + "/" + this.maxhp);
        ((ProgressBar)rootView.findViewById(R.id.hp_progressBar)).setMax(this.maxhp);
        ((ProgressBar)rootView.findViewById(R.id.hp_progressBar)).setProgress(this.hp);

        ((TextView)rootView.findViewById(R.id.ep_progressText)).setText(this.exp + "/" + (this.exp + this.expToNext));
        ((ProgressBar)rootView.findViewById(R.id.ep_progressBar)).setMax(this.exp + this.expToNext);
        ((ProgressBar)rootView.findViewById(R.id.ep_progressBar)).setProgress(this.exp);

        ((TextView)rootView.findViewById(R.id.level_progressText)).setText(this.level + "/" + 10);
        ((ProgressBar)rootView.findViewById(R.id.level_progressBar)).setMax(10);
        ((ProgressBar)rootView.findViewById(R.id.level_progressBar)).setProgress(this.level);
    }

    /**
     *
     * @param abilityNumber
     */
    private void showAbilityDescription(int abilityNumber){
        MainActivity.Ability ability = MainActivity.abilities[MainActivity.chosenClassIDs[abilityNumber-1]];
        String abilityDescription = "" +
                ability.name + "\n" +
                ability.description + "\n" +
                "Cooldown: " + ability.cooldown + " seconds";
        Toast.makeText(MainActivity.getInstance().getApplicationContext(), abilityDescription,
                Toast.LENGTH_LONG).show();
    }
}
