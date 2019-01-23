package com.example.augus.comanda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class Main3Activity extends ArrayAdapter{
    Context contexto;
    int id;
    List<Map<String, Object>> produtos;

    public Main3Activity(AdapterView.OnItemSelectedListener contexto, int id, List<Map<String, Object>> produtos) {
        super((Context) contexto, id);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;
        TextView textView_Descricao;

        if(view == null){
            LayoutInflater inflater = LayoutInflater.from(contexto);
            view = inflater.inflate(id, parent, false);
        }

        //textView_Descricao = view.findViewById(R.id.textView_Descricao);

        Map<String, Object> produto = produtos.get(position);

        //textView_Descricao.setText(produto.get("descricao_produto").toString());
        return view;
    }
}
