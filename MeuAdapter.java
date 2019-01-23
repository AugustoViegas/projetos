package com.example.augus.comanda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

/**
 * Created by augus on 20/02/2018.
 */

public class MeuAdapter extends ArrayAdapter {
    Context context;
    int id;
    Button btn_Decrementa;
    List<Map<String, Object>> produtos;
    MainActivity mainActivity;


    public MeuAdapter(Context context, List<Map<String, Object>> data, int id) {
        super(context, id, data);

        this.context = context;
        this.id = id;
        this.produtos = data;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;

        if(view == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(id, parent, false);
        }

        Toast.makeText(getContext(), "TESTEEEE", Toast.LENGTH_LONG).show();

        /*btn_Decrementa = view.findViewById(R.id.button_Decrementa);

        btn_Decrementa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mainActivity.decrementaItem(view);

                Toast.makeText(getContext(), "TESTEEEE", Toast.LENGTH_LONG).show();
            }
        });*/


        //textView_Descricao = view.findViewById(R.id.textView_Descricao);

        //Map<String, Object> produto = produtos.get(position);

        //textView_Descricao.setText(produto.get("descricao_produto").toString());
        return view;
    }
}
