package com.example.augus.comanda;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Objects;

public class Main6Activity extends AppCompatActivity {
    private DataBase dataBase;
    private SQLiteDatabase db;
    private EditText txt_ip, txt_data, txt_numeroCelular, txt_nomeRede, txt_endereco, txt_usuario, txt_senha, txt_porta;
    private Spinner spinner_tipoEnvio;
    private Button button_cancela, button_salva;

    String modoEnvio;
    int tipoEnvio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); //Mostrar o botão
            Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);      //Ativar o botão
            Objects.requireNonNull(getSupportActionBar()).setTitle("Configurações");
        }

        dataBase = new DataBase(this);
        db = dataBase.getWritableDatabase();

        txt_ip = findViewById(R.id.txt_ip);
        txt_data = findViewById(R.id.txt_data);
        txt_numeroCelular = findViewById(R.id.txt_numeroCelular);
        txt_nomeRede = findViewById(R.id.txt_nomeRede);
        txt_endereco = findViewById(R.id.txt_endereco);
        txt_usuario = findViewById(R.id.txt_usuario);
        txt_senha = findViewById(R.id.txt_senha);
        txt_porta = findViewById(R.id.txt_porta);

        txt_ip.setSingleLine(true);
        txt_ip.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_CLASS_TEXT);

        txt_data.setSingleLine(true);
        txt_data.setInputType(InputType.TYPE_CLASS_NUMBER);
        txt_data.addTextChangedListener(Mask.insert("##/##/####", txt_data)); //máscara para data no editText

        txt_numeroCelular.setSingleLine(true);
        txt_numeroCelular.setInputType(InputType.TYPE_CLASS_NUMBER);

        txt_nomeRede.setSingleLine(true);

        txt_endereco.setSingleLine(true);
        txt_endereco.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        txt_usuario.setSingleLine(true);
        txt_usuario.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        txt_senha.setSingleLine(true);
        txt_senha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        txt_porta.setSingleLine(true);
        txt_porta.setInputType(InputType.TYPE_CLASS_NUMBER);

        button_cancela = findViewById(R.id.button_cancela);
        button_salva = findViewById(R.id.button_salva);

        spinner_tipoEnvio = findViewById(R.id.spinner_tipoEnvio);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.modo_envio,
                android.R.layout.simple_spinner_dropdown_item);

        spinner_tipoEnvio.setAdapter(arrayAdapter);

        spinner_tipoEnvio.setSelection(selectDadosConfiguracoes());

        spinner_tipoEnvio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                modoEnvio = adapterView.getItemAtPosition(i).toString();
                tipoEnvio = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //Botão adicional na ToolBar
        switch (item.getItemId()) {
            case android.R.id.home:  //ID do seu botão (gerado automaticamente pelo android, usando como está, deve funcionar
                this.finish();
                break;
            default:break;
        }
        return true;
    }

    @Override
    public void onBackPressed(){ //Botão BACK padrão do android
        this.finish(); //Método para matar a activity e não deixa-lá indexada na pilhagem
    }

    public void fechaConfiguracoes(View view) {
        this.finish();
        //Intent intent = new Intent(this, Main2Activity.class);
        //startActivity(intent);
    }

    public void salvaConfiguracoes(View view) {
        ContentValues contentValues = new ContentValues();

        String ip = txt_ip.getText().toString().trim();
        String data = txt_data.getText().toString().trim();
        String numeroCel = txt_numeroCelular.getText().toString().trim();
        String nomeRede = txt_nomeRede.getText().toString().trim();
        String endereco = txt_endereco.getText().toString().trim();
        String usuario = txt_usuario.getText().toString().trim();
        String senha = txt_senha.getText().toString().trim();
        int porta =  Integer.parseInt(txt_porta.getText().toString().trim());

        if (!selectConfiguracoes()) {
            contentValues.put("ip_servidor", ip);
            contentValues.put("data", data);
            contentValues.put("nomeCelular", numeroCel);
            contentValues.put("nomeRede", nomeRede);
            contentValues.put("endereco_ftp", endereco);
            contentValues.put("usuario_ftp", usuario);
            contentValues.put("senha_ftp", senha);
            contentValues.put("porta_ftp", porta);
            contentValues.put("tipo_envio", tipoEnvio);

            db.insert(dataBase.TABLE_NAME8, null, contentValues);

            Toast.makeText(getApplicationContext(), "Informações salvas com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            contentValues.put("ip_servidor", ip);
            contentValues.put("data", data);
            contentValues.put("nomeCelular", numeroCel);
            contentValues.put("nomeRede", nomeRede);
            contentValues.put("endereco_ftp", endereco);
            contentValues.put("usuario_ftp", usuario);
            contentValues.put("senha_ftp", senha);
            contentValues.put("porta_ftp", porta);
            contentValues.put("tipo_envio", tipoEnvio);

            db.update(dataBase.TABLE_NAME8, contentValues, "codigo = 1", null);

            Toast.makeText(getApplicationContext(), "Informações alteradas com sucesso!", Toast.LENGTH_SHORT).show();
        }

        fechaConfiguracoes(view);
    }

    public boolean selectConfiguracoes() {
        boolean achou;
        int codigo = 0;

        String select = "SELECT codigo FROM " + dataBase.TABLE_NAME8;

        Cursor cursor1 = db.rawQuery(select, null);
        cursor1.moveToFirst();

        for (int c = 0; c < cursor1.getCount(); c++) {
            codigo = Integer.parseInt(cursor1.getString(0));
            cursor1.moveToNext();
        }
        cursor1.close();

        if (codigo == 1) {
            achou = true;
        } else {
            achou = false;
        }

        return achou;
    }

    public int selectDadosConfiguracoes() {
        int tipo = 0;
        
        String select = "SELECT data, nomeCelular, nomeRede, ip_servidor, endereco_ftp, usuario_ftp, senha_ftp, porta_ftp, tipo_envio FROM " + dataBase.TABLE_NAME8 +
                " WHERE codigo = 1";

        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int c = 0; c < cursor.getCount(); c++) {
            txt_data.setText(cursor.getString(0));
            txt_numeroCelular.setText(cursor.getString(1));
            txt_nomeRede.setText(cursor.getString(2));
            txt_ip.setText(cursor.getString(3));
            txt_endereco.setText(cursor.getString(4));
            txt_usuario.setText(cursor.getString(5));
            txt_senha.setText(cursor.getString(6));
            txt_porta.setText(String.valueOf(cursor.getInt(7)));
            tipo = cursor.getInt(8);
            cursor.moveToNext();
        }
        cursor.close();
        
        return tipo;
    }
}
