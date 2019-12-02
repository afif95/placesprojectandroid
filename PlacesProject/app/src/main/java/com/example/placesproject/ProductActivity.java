package com.example.placesproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ProductActivity extends AppCompatActivity {

    private TextView textView;
    private Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        textView=findViewById(R.id.producttext);
        back=findViewById(R.id.back_button);
        Bundle ob=getIntent().getExtras();
        if(ob!=null){
            int i=ob.getInt("tag");
            if (i == 1) {
                textView.setText("Recommended products:\n\n\n" +
                        "jeans_pant\n" +
                        "shirt\n" +
                        "panjabi\n" +
                        "jamdani_sharee\n" +
                        "shoe\n" +
                        "half_pant\n" +
                        "sock\n" +
                        "cotton_shalwar_kameez\n" +
                        "cotton_panjabi\n" +
                        "faux_leather_sandal\n" +
                        "tie\n" +
                        "belt\n" +
                        "formal_shirt\n\n\n\n"+
                        "bisshonobi\n" +
                        "paradoxical_sajid\n" +
                        "stop_learning\n" +
                        "nandito_naroke\n" +
                        "kichukkhon\n" +
                        "muntakhab_hadis\n" +
                        "think_grow\n" +
                        "turning_points\n" +
                        "desception_points\n" +
                        "student_hack\n" +
                        "vai_re_apu_re\n" +
                        "oshomapto_atto\n" +
                        "fuler_kata\n" +
                        "canvas\n" +
                        "vallegena\n" +
                        "oshukh\n" +
                        "opekkha\n" +
                        "bidda_koushal\n" +
                        "majic_shikhun\n" +
                        "dusshahosik\n" +
                        "ma_ma_ma_baba\n" +
                        "fera\n" +
                        "himu_somogro\n" +
                        "shahadat_najater\n");
            }
            else if(i == 2){
                textView.setText("Recommended products:\n\n\n" +
                        "refrigerator\n" +
                        "television\n" +
                        "radio\n" +
                        "tubelight\n" +
                        "ricecooker\n" +
                        "electric_heater\n" +
                        "air_cooler\n" +
                        "water_heater\n" +
                        "air_conditioner\n" +
                        "stove\n" +
                        "electric_switch\n" +
                        "dish_antena\n\n\n\n" +
                        "The Simulation\n" +
                        "Live at the Ramblin' Man Fair\n" +
                        "Magnolia\n" +
                        "Look Alive\n" +
                        "A Real Good Kid\n" +
                        "Remind Me Tomorrow\n" +
                        "Better Oblivion Community Center\n" +
                        "Why You So Crazy\n" +
                        "Prismism\n" +
                        "Power Chords\n" +
                        "A Good Friend Is Nice\n" +
                        "The Great Adventure\n" +
                        "Feral Roots\n" +
                        "Toast to Our Differences\n" +
                        "Poran Kande\n" +
                        "Shudhu Tomar Jonyo\n" +
                        "Konttho\n" +
                        "Gotro\n" +
                        "Raabta - Bengali Version\n" +
                        "Shomoy\n" +
                        "Vinci Da");
            }
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ob = new Intent(ProductActivity.this,MapActivity.class);
                startActivity(ob);
                finish();
            }
        });
    }
}
