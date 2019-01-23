package com.example.augus.comanda;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private DataBase dataBase;
    private Spinner spinner_Grupos;
    private ListView listView_Produtos, listView_Comanda;
    private List<String> grupos =  new ArrayList<String>();
    private List<Map<String,Object>> produtos, produtos_Comanda, produtos_Descricao, teste, produtos_Enviados;
    private String grupo;
    private String produto, produto_Selecionado;
    private EditText txt_nComanda, txt_nMesa;
    private TextView  textView_Pedidos;
    private Button btn_Delete, btn_Salvar, btn_Decrementa, btn_VerComanda;
    private AlertDialog alerta;
    String nome;

    ProgressDialog progress;

    private static ArrayList<String> opcoes = new ArrayList<String>() {
        {
            add("Diminuir quantidade");
            add("Excluir");
            add("Observação");
        }
    };

    String [] de = {"descricao_produto"};
    int [] para = {R.id.textView_Descricao};
    String [] de_Pedidos = {"qtde_item", "descricao_produto"};
    int [] para_Pedidos = {R.id.textView_Qtde, R.id.textView_Pedidos};
    int qtdeItem = 0;
    int x;
    int o = 0;
    int pos = 0;
    int posicao_Anterior = 0;
    int maior = 0;
    int posicaoArray = 0;
    boolean duasCasas;
    File caminhoOrigem;
    int posicao_Ocupada;
    int posicaoAnterior = -1;
    int posicaoBloqueada = -1;
    String ip;

    String [] qtdeI = new String [9000];
    String [] enviado = new String [9000];
    int [] posicoes = new int [9000];
    String [] posicoes_ocupadas = new String [9000];
    String [] grupo_Produtos = new String [9000];
    String [] prod_Enviados = new String [9000];
    String [] produtos_Nao_Enviados = new String [9000];
    String [] itensObservacoes = new String [9000];
    String [] observacoes = new String [9000];

    View viewSelecionada;

    private static final String TAG = "MyFTPClientFunctions";
    MyFTPClientFunctions myFTPClientFunctions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getSupportActionBar()).hide();
        }

        setContentView(R.layout.activity_main);

        produtos_Descricao = new ArrayList<>();
        teste = new ArrayList<>();

        bindFields();

        dataBase = new DataBase(this);
        db = dataBase.getWritableDatabase();

        txt_nComanda = findViewById(R.id.txt_nComanda);
        txt_nMesa = findViewById(R.id.txt_nMesa);

        //btn_Delete = findViewById(R.id.button_Delete);
        btn_Salvar = findViewById(R.id.button_SalvarComanda);
        //btn_Decrementa = findViewById(R.id.button_Decrementa);
        btn_VerComanda = findViewById(R.id.button_verComanda);

        Permission permission = new Permission();
        permission.verifyStoragePermissions(this);

        txt_nComanda.setOnKeyListener(new AdapterView.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    validaNComanda();
                }
                return false;
            }
        });


        txt_nMesa.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view2, int o, KeyEvent keyEvent2) {
                if ((keyEvent2.getAction() == KeyEvent.ACTION_DOWN) && (o == KeyEvent.KEYCODE_ENTER)) {
                    validaNMesa();
                }
                return false;
            }
        });

        grupos.add("GRUPOS");

        String query = "SELECT nome FROM " + dataBase.TABLE_NAME2 + " WHERE codigo IN (SELECT grupo_produto from " + dataBase.TABLE_NAME + ")";

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++){
            String desc = cursor.getString(0);
            grupo_Produtos[i] = desc;
            cursor.moveToNext();
        }
        cursor.close();

        for (int i = 0; i < cursor.getCount(); i++){
            grupos.add(grupo_Produtos[i]);

            spinner_Grupos = findViewById(R.id.spinner_Grupos);

            //spinner_Grupos.getBackground().setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.SRC_ATOP);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, grupos);

            ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;

            spinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner);

            spinner_Grupos.setAdapter(spinnerArrayAdapter);
        }

        if (spinner_Grupos != null) {
            spinner_Grupos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onItemSelected(AdapterView<?> parent, View v, int posicao, long id) {
                    grupo = parent.getItemAtPosition(posicao).toString();

                    if (spinner_Grupos.getSelectedItem() == "GRUPOS") {
                        produtos = new ArrayList<>();

                        SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), produtos, R.layout.activity_main3, de, para);

                        listView_Produtos.setAdapter(simpleAdapter);
                    } else {
                        String query = "SELECT nome FROM " + dataBase.TABLE_NAME + " WHERE" +
                                " grupo_produto = (SELECT codigo FROM " + dataBase.TABLE_NAME2 + " WHERE nome = '" + grupo + "')";

                        produtos = exec(query);

                        SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), produtos, R.layout.activity_main3, de, para);

                        listView_Produtos.setAdapter(simpleAdapter);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Atualizar grupos e produtos novamente", Toast.LENGTH_SHORT).show();

            this.finish();

            //Intent intent = new Intent(this, Main2Activity.class);
            //startActivity(intent);
        }

        FloatingActionButton fab = findViewById(R.id.fab_salva_comanda);
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                salvarComanda(view);
            }
        });

        FloatingActionButton fab2 = findViewById(R.id.fab_visualiza_comanda);
        fab2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                visualizaComanda(view);
            }
        });

        int abrir = Util.getValueByExtra(getIntent(), "abrir",  Integer.class);
        if (abrir == 1) {
            esconderTeclado();
            txt_nComanda.setText(Util.getValueByExtra(getIntent(), "comanda", String.class));
            buscaComanda();
        }

        myFTPClientFunctions = new MyFTPClientFunctions();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private void bindFields(){
        listView_Produtos = initListView();
        listView_Comanda = listComanda();
    }

    public void alertCarga(final MainActivity mainActivity) {
        progress = new ProgressDialog(this);

        progress.setMessage("Enviando...");

        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        progress.show();

        progress.setCanceledOnTouchOutside(false);

        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                progress.cancel();
                mainActivity.finish();
                Toast.makeText(getApplicationContext(), "Enviado com sucesso", Toast.LENGTH_SHORT).show();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 5000);
    }

    private ListView initListView(){
        final ListView list = findViewById(R.id.listView_Produtos);

        list.setClickable(true);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posicao, long l) { //aqui ele vai jogar o item selecionado para a listView_Comanda, para ser enviado para o servidor
                int achou = 0;

                produto = parent.getItemAtPosition(posicao).toString();

                String select = "SELECT descricao_produto FROM " + dataBase.TABLE_NAME1 + " WHERE numero_comanda = '" + txt_nComanda.getText() + "' and" +
                        " numero_mesa = '" + txt_nMesa.getText() + "' and enviado = 'Sim' GROUP BY descricao_produto";

                Cursor cursor = db.rawQuery(select, null);
                cursor.moveToFirst();

                for (int i = 0; i < cursor.getCount(); i++) {
                    String desc = cursor.getString(0);
                    prod_Enviados[i] = desc;
                    cursor.moveToNext();
                }
                cursor.close();

                /*String select_grupo = "SELECT nome FROM " + dataBase.TABLE_NAME2 + " WHERE codigo IN (SELECT grupo_produto FROM " + dataBase.TABLE_NAME + " WHERE descricao_produto = '" + produto.substring(19, produto.length() - 1) + "')";

                Cursor cursor_grupo = db.rawQuery(select_grupo, null);
                cursor_grupo.moveToFirst();

                String grupo = null;

                for (int i = 0; i < cursor_grupo.getCount(); i++) {
                    grupo = cursor_grupo.getString(0);
                    cursor_grupo.moveToNext();
                }
                cursor_grupo.close();*/

                posicoes[x] = x;

                if (!teste.toString().substring(1, teste.toString().length() - 1).contains(produto) /*|| grupo.equalsIgnoreCase("PIZZAS")*/) {
                    qtdeItem = 1;

                    String query = "SELECT codigo FROM " + dataBase.TABLE_NAME + " WHERE nome = '" + produto.substring(19, produto.length() - 1) + "'";

                    produtos_Comanda = exec2(query);

                    String query_descricao = "SELECT nome FROM " + dataBase.TABLE_NAME + " WHERE codigo = '" +
                        produtos_Comanda.toString().substring(9, produtos_Comanda.toString().length() - 2) + "'";

                    produtos_Descricao = exec3(query_descricao);
                    teste = exec4(query_descricao);

                    SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), produtos_Descricao, R.layout.activity_main4, de_Pedidos, para_Pedidos);

                    listView_Comanda = findViewById(R.id.listView_Comanda);

                    listView_Comanda.setAdapter(simpleAdapter);

                    listView_Comanda.setSelection(x);

                    qtdeI[x] = "0" + "," + "1" + "," + produto.substring(19, produto.length() - 1);

                    x = x + 1;

                    produtoNaoEnviado();

                } else if (teste.toString().substring(1, teste.toString().length() - 1).contains(produto)) { //aqui ele insere o item na lista caso ele já tenha sido enviado para impressão
                    for (int z = 0; z < posicaoArray; z++){
                        if (produtos_Nao_Enviados[z] != null) {
                            if (produto.substring(19, produto.length() - 1).equalsIgnoreCase(produtos_Nao_Enviados[z])) {
                                achou = 1;
                            }
                        }
                    }

                    if (achou == 0) {
                        for (int i = 0; i < x; i++) {
                            if (prod_Enviados[i] != null) {
                                if (prod_Enviados[i].equalsIgnoreCase(produto.substring(19, produto.length() - 1))) {
                                    qtdeItem = 1;

                                    String query = "SELECT codigo FROM " + dataBase.TABLE_NAME + " WHERE nome = '" + produto.substring(19, produto.length() - 1) + "'";

                                    produtos_Comanda = exec2(query);

                                    String query_descricao = "SELECT nome FROM " + dataBase.TABLE_NAME + " WHERE codigo = '" +
                                            produtos_Comanda.toString().substring(9, produtos_Comanda.toString().length() - 2) + "'";

                                    produtos_Descricao = exec3(query_descricao);
                                    teste = exec4(query_descricao);

                                    SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), produtos_Descricao, R.layout.activity_main4, de_Pedidos, para_Pedidos);

                                    listView_Comanda = findViewById(R.id.listView_Comanda);

                                    listView_Comanda.setAdapter(simpleAdapter);

                                    listView_Comanda.setSelection(x);

                                    qtdeI[x] = "0" + "," + "1" + "," + produto.substring(19, produto.length() - 1);

                                    x = x + 1;

                                    produtoNaoEnviado();
                                }
                            }
                        }
                    }
                }
            }
        });

        list.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch (View v, MotionEvent motionEvent){
                if (listView_Produtos.getLastVisiblePosition() == -1) {
                    listView_Produtos.requestDisallowInterceptTouchEvent(false);
                } else {
                    listView_Produtos.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

        return list;
    }

    private ListView listComanda(){
        final ListView listComanda = findViewById(R.id.listView_Comanda);
        //listComanda.setClickable(true);

        listComanda.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posicao, long l) {
                int pOcupado = 0;

                produto = parent.getItemAtPosition(posicao).toString();

                Map<String, Object> add = produtos_Descricao.get(posicao);

                buscaPosicao();

                for (int i = 0; i < posicoes_ocupadas.length; i++) {
                    if (posicoes_ocupadas[i] != null) {
                        if (posicoes_ocupadas[i].equalsIgnoreCase(String.valueOf(posicao))){
                            pOcupado = 1;
                        }
                    }
                }

                /*String select_grupo = "SELECT nome FROM " + dataBase.TABLE_NAME2 + " WHERE codigo IN (SELECT grupo_produto FROM " + dataBase.TABLE_NAME + " WHERE descricao_produto = '" + produto.substring(19, produto.length() - 14) + "')";

                Cursor cursor_grupo = db.rawQuery(select_grupo, null);
                cursor_grupo.moveToFirst();

                String grupo = null;

                for (int i = 0; i < cursor_grupo.getCount(); i++) {
                    grupo = cursor_grupo.getString(0);
                    cursor_grupo.moveToNext();
                }
                cursor_grupo.close();*/

                //if (grupo != null && grupo.equalsIgnoreCase("PIZZAS")) {

                //} else {
                    if (pOcupado != 1) {
                        //teste para saber se a quantidade do item selecionado de uma posição diferente é maior ou igual a 10 para cair no if certo
                        // e fazer as alterações necessárias.
                        if (posicaoBloqueada == -1) {
                            //if (posicao != posicao_Anterior) {
                                //posicao_Anterior = posicao;

                                for (int z = 0; z < x; z++) {
                                    if (qtdeI[z] != null) {
                                        if (duasCasas == false && ((produto.substring(19, produto.length() - 14).equalsIgnoreCase(qtdeI[z].substring(4)) || produto.substring(19, produto.length() - 15).equalsIgnoreCase(qtdeI[z].substring(5))))) {
                                            if (Integer.parseInt(qtdeI[z].substring(0, 1)) == 1) {
                                                duasCasas = true;
                                            } else {
                                                duasCasas = false;
                                            }
                                        }
                                        if (duasCasas == true && ((produto.substring(19, produto.length() - 15).equalsIgnoreCase(qtdeI[z].substring(5)) || produto.substring(19, produto.length() - 14).equalsIgnoreCase(qtdeI[z].substring(4))))) {
                                            if (Integer.parseInt(qtdeI[z].substring(0, 1)) == 0) {
                                                duasCasas = false;
                                            } else {
                                                duasCasas = true;
                                            }
                                        }
                                    }
                                }
                            //}
                            posicao_Anterior = posicao;

                            for (int i = 0; i < x; i++) {
                                int q = 0;

                                if (qtdeI[i] != null) {
                                    if (duasCasas == false) {
                                        if (produto.substring(19, produto.length() - 14).equalsIgnoreCase(qtdeI[i].substring(4))) {
                                            q = Integer.parseInt(qtdeI[i].substring(2, 3));

                                            int at = q + 1;

                                            testaTamanho(at);

                                            add.put("qtde_item", at);

                                            produtos_Descricao.set(posicao, add);

                                            SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), produtos_Descricao, R.layout.activity_main4, de_Pedidos, para_Pedidos);

                                            listView_Comanda = findViewById(R.id.listView_Comanda);

                                            listView_Comanda.setAdapter(simpleAdapter);

                                            listView_Comanda.setSelection(posicao);

                                            qtdeI[i] = maior + "," + at + "," + produto.substring(19, produto.length() - 14);

                                            if (at == 10) {
                                                duasCasas = true;
                                            }
                                        }
                                    } else if (duasCasas == true) {
                                        if (produto.substring(19, produto.length() - 15).equalsIgnoreCase(qtdeI[i].substring(5))) {
                                            q = Integer.parseInt(qtdeI[i].substring(2, 4));

                                            int at = q + 1;

                                            testaTamanho(at);

                                            add = produtos_Descricao.get(posicao);

                                            add.put("qtde_item", at);

                                            produtos_Descricao.set(posicao, add);

                                            SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), produtos_Descricao, R.layout.activity_main4, de_Pedidos, para_Pedidos);

                                            listView_Comanda = findViewById(R.id.listView_Comanda);

                                            listView_Comanda.setAdapter(simpleAdapter);

                                            listView_Comanda.setSelection(posicao);

                                            qtdeI[i] = maior + "," + at + "," + produto.substring(19, produto.length() - 15);
                                        }
                                    }
                                }
                            }
                        }
                    }
                //}
            }
        });

        //deixo um item selecionado para ser diminuido, excluído ou para ter uma observação inserida
        listComanda.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int posicao, long l) {
                int pOcupado = 0;
                int pTeste;
                boolean retorno = false;

                pos = adapterView.getPositionForView(view);
                nome = adapterView.getItemAtPosition(pos).toString().substring(19, adapterView.getItemAtPosition(pos).toString().length() - 5).replaceAll("[-abcdefghijklmnopqrstuvwxyz,_{}.=]", "").trim();

                pTeste = adapterView.getPositionForView(view);
                if (posicaoBloqueada == pTeste || posicaoBloqueada == -1) {
                    pos = adapterView.getPositionForView(view);
                    nome = adapterView.getItemAtPosition(pos).toString().substring(19, adapterView.getItemAtPosition(pos).toString().length() - 5).replaceAll("[-abcdefghijklmnopqrstuvwxyz,_{}.=]", "").trim();

                    buscaPosicao();

                    for (int i = 0; i < posicoes_ocupadas.length; i++) {
                        if (posicoes_ocupadas[i] != null) {
                            if (posicoes_ocupadas[i].equalsIgnoreCase(String.valueOf(pos))) {
                                pOcupado = 1;
                            }
                        }
                    }

                    if (pOcupado != 1) {
                        if (posicaoAnterior == pos) {
                            produto_Selecionado = null;

                            posicaoAnterior = -1;

                            posicaoBloqueada = -1;

                            spinner_Grupos.setEnabled(true);

                            listView_Produtos.setEnabled(true);

                            view.setSelected(false);

                            view.setBackgroundColor(Color.TRANSPARENT);

                            retorno = true;
                        } else {
                            produto_Selecionado = adapterView.getItemAtPosition(pos).toString();

                            viewSelecionada = view;

                            posicaoAnterior = pos;

                            posicaoBloqueada = pos;

                            spinner_Grupos.setEnabled(false);

                            listView_Produtos.setEnabled(false);

                            view.setSelected(true);

                            view.setBackgroundColor(Color.GRAY);

                            retorno = true;

                            menuItem(viewSelecionada);
                        }
                    } else {
                        retorno = false;
                    }
                }
                return retorno;
            }
        });

        /*listComanda.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                int pOcupado = 0;

                buscaPosicao();

                for (int i = 0; i < posicoes_ocupadas.length; i++) {
                    if (posicoes_ocupadas[i] != null) {
                        if (posicoes_ocupadas[i].equalsIgnoreCase(String.valueOf(pos))) {
                            pOcupado = 1;
                            break;
                        }
                    }
                }

                if (pOcupado != 1) {
                    contextMenu.add(Menu.NONE, 1, Menu.NONE, "Diminuir");
                    contextMenu.add(Menu.NONE, 2, Menu.NONE, "Excluir");
                    contextMenu.add(Menu.NONE, 3, Menu.NONE, "Observação");
                } else {
                    Toast.makeText(getApplicationContext(), "Item já enviado!", Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        listComanda.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch (View v, MotionEvent motionEvent){
                if (listView_Comanda.getLastVisiblePosition() == -1) {
                    listView_Comanda.requestDisallowInterceptTouchEvent(false);
                } else {
                    listView_Comanda.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

        return listComanda;
    }

    /*@Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        int position = menuInfo.position;
        pos = position;

        Log.v("MENU", String.valueOf(position));

        switch (item.getItemId()) {
            case 1: ;
            case 2: deletaItem();
        }


        return super.onContextItemSelected(item);
    }*/

    public void menuItem(final View view) {
        final CharSequence[] op = opcoes.toArray(new String[opcoes.size()]);

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String produto = produto_Selecionado.substring(19, produto_Selecionado.length() - 14);
        final String mensagem;

        if (produto.substring(produto.length() - 1).equalsIgnoreCase(",")){
            mensagem = produto_Selecionado.substring(19, produto_Selecionado.length() - 15);
        } else {
            mensagem = produto_Selecionado.substring(19, produto_Selecionado.length() - 14);
        }

        //define o titulo
        builder.setTitle(mensagem);

        builder.setItems(op, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(DialogInterface dialogInterface, int selecionado) {
                if (selecionado == 0) {
                    decrementaItem(view);
                } else if (selecionado == 1) {
                    deletaItem(view);
                } else if (selecionado == 2){
                    obsItem(view);
                }
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                produto_Selecionado = null;

                posicaoAnterior = -1;

                posicaoBloqueada = -1;

                spinner_Grupos.setEnabled(true);

                listView_Produtos.setEnabled(true);

                view.setSelected(false);

                view.setBackgroundColor(Color.TRANSPARENT);

                Toast.makeText(MainActivity.this, "Operação cancelada", Toast.LENGTH_SHORT).show();
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);
    }

    public void produtoNaoEnviado() {
        produtos_Nao_Enviados[posicaoArray] = produto.substring(19, produto.length() - 1);

        //incremento a variável posicaoArray para não inserir em uma posição que já tenha algum item
        posicaoArray = posicaoArray + 1;
    }

    public void testaTamanho(int at) {
        if(at >= 10){
            maior = 1;
        }else{
            maior = 0;
        }
    }

    public void deletaItem(View view) {
        if (listView_Comanda.getLastVisiblePosition() != -1) {
            if (produto_Selecionado != null) {
                for(int i = 0; i < x; i++) {
                    if (qtdeI[i] != null) {
                        if (produto_Selecionado.substring(19, produto_Selecionado.length() - 14).equalsIgnoreCase(qtdeI[i].substring(4)) || produto_Selecionado.substring(19, produto_Selecionado.length() - 15).equalsIgnoreCase(qtdeI[i].substring(5))) {
                            teste.remove(pos);

                            produtos_Descricao.remove(pos);

                            arrumaPosicao(pos);

                            deletaObsArray();

                            for (int z = i; z < x; z++) {
                                String aux;

                                aux = qtdeI[z];

                                qtdeI[z] = qtdeI[z + 1];
                            }

                            posicaoAnterior = -1;

                            posicaoBloqueada = -1;

                            listView_Produtos.setEnabled(true);

                            spinner_Grupos.setEnabled(true);

                            SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), produtos_Descricao, R.layout.activity_main4, de_Pedidos, para_Pedidos);

                            listView_Comanda = findViewById(R.id.listView_Comanda);

                            listView_Comanda.setAdapter(simpleAdapter);

                            x = x - 1;

                            listView_Comanda.setSelection(x);
                        }
                    }
                }
                produto_Selecionado = null;
            } else {
                Toast.makeText(getApplicationContext(), "Selecione um item para excluí-lo", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Comanda vazia", Toast.LENGTH_SHORT).show();
        }
    }

    public void deletaObsArray() {
        for (int i = 0; i < itensObservacoes.length; i++) {
            if (itensObservacoes[i] != null) {
                if (nome.equalsIgnoreCase(itensObservacoes[i])) {
                    itensObservacoes[i] = null;
                    observacoes[i] = null;
                }
            }
        }
    }

    public void arrumaPosicao(int posicao) {
        int p;

        ContentValues contentValues = new ContentValues();

        String select = "SELECT posicao FROM " + dataBase.TABLE_NAME7 + " WHERE num_Comanda = " + Integer.parseInt(txt_nComanda.getText().toString()) + " AND num_Mesa = " + Integer.parseInt(txt_nMesa.getText().toString()) + " AND posicao >= " + posicao;

        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            p = cursor.getInt(0);

            if (p == posicao) {
                db.delete(dataBase.TABLE_NAME7, "num_Comanda = " + Integer.parseInt(txt_nComanda.getText().toString()) + " AND num_Mesa = " + Integer.parseInt(txt_nMesa.getText().toString()) + " AND posicao = " + posicao, null);
            } else {
                //int po = p - 1;
                contentValues.put("posicao", p - 1);
                db.update(dataBase.TABLE_NAME7, contentValues, " num_Comanda = " + Integer.parseInt(txt_nComanda.getText().toString()) + " AND num_Mesa = " + Integer.parseInt(txt_nMesa.getText().toString()) + " AND posicao = " + p, null);
            }
            cursor.moveToNext();
        }
        cursor.close();
    }

    public void decrementaItem(View view){
        int maiorDecrementa = 0;

        Map<String, Object> add;

        if (listView_Comanda.getLastVisiblePosition() != -1) {
            add = produtos_Descricao.get(pos);

            if (produto_Selecionado != null) {
                for (int i = 0; i < x; i++) {
                    if (qtdeI[i] != null) {
                        if (produto_Selecionado.substring(19, produto_Selecionado.length() - 14).equalsIgnoreCase(qtdeI[i].substring(4))) {
                            maiorDecrementa = Integer.parseInt(qtdeI[i].substring(0, 1));
                        } else if (produto_Selecionado.substring(19, produto_Selecionado.length() - 15).equalsIgnoreCase(qtdeI[i].substring(5))) {
                            maiorDecrementa = Integer.parseInt(qtdeI[i].substring(0, 1));
                        }
                    }
                }

                for (int i = 0; i < x; i++) {
                    int q = 0;

                    if (maiorDecrementa == 0) {
                        if (qtdeI[i] != null) {
                            if (produto_Selecionado.substring(19, produto_Selecionado.length() - 14).equalsIgnoreCase(qtdeI[i].substring(4))) {
                                q = Integer.parseInt(qtdeI[i].substring(2, 3));

                                if (q <= 1) {
                                    posicaoAnterior = -1;

                                    posicaoBloqueada = -1;

                                    listView_Produtos.setEnabled(true);

                                    spinner_Grupos.setEnabled(true);

                                    viewSelecionada.setSelected(false);

                                    viewSelecionada.setBackgroundColor(Color.TRANSPARENT);
                                } else {
                                    int at = q - 1;

                                    testaTamanho(at);

                                    add.put("qtde_item", at);

                                    produtos_Descricao.set(pos, add);

                                    SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), produtos_Descricao, R.layout.activity_main4, de_Pedidos, para_Pedidos);

                                    listView_Comanda = findViewById(R.id.listView_Comanda);

                                    listView_Comanda.setAdapter(simpleAdapter);

                                    listView_Comanda.setSelection(pos);

                                    qtdeI[i] = maior + "," + at + "," + produto_Selecionado.substring(19, produto_Selecionado.length() - 14);

                                    view.setSelected(false);

                                    posicaoAnterior = -1;

                                    posicaoBloqueada = -1;

                                    listView_Produtos.setEnabled(true);

                                    spinner_Grupos.setEnabled(true);
                                }
                            }
                        }
                    } else if (maiorDecrementa == 1) {
                        if (qtdeI[i] != null) {
                            if (produto_Selecionado.substring(19, produto_Selecionado.length() - 15).equalsIgnoreCase(qtdeI[i].substring(5))) {
                                q = Integer.parseInt(qtdeI[i].substring(2, 4));

                                int at = q - 1;

                                testaTamanho(at);

                                add = produtos_Descricao.get(pos);

                                add.put("qtde_item", at);

                                produtos_Descricao.set(pos, add);

                                SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), produtos_Descricao, R.layout.activity_main4, de_Pedidos, para_Pedidos);

                                listView_Comanda = findViewById(R.id.listView_Comanda);

                                listView_Comanda.setAdapter(simpleAdapter);

                                listView_Comanda.setSelection(pos);

                                qtdeI[i] = maior + "," + at + "," + produto_Selecionado.substring(19, produto_Selecionado.length() - 15);

                                view.setSelected(false);

                                posicaoAnterior = -1;

                                posicaoBloqueada = -1;

                                listView_Produtos.setEnabled(true);

                                spinner_Grupos.setEnabled(true);

                                if (Integer.parseInt(qtdeI[i].substring(0, 1)) == 0) {
                                    duasCasas = false;
                                }
                            }
                        }
                    }
                }
                produto_Selecionado = null;
            } else {
                Toast.makeText(getApplicationContext(), "Selecione um item para diminuir a quantidade", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Comanda vazia", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void obsItem(View view) {
        if (listView_Comanda.getLastVisiblePosition() != -1){
            if (produto_Selecionado != null) {
                observacao(pos);

                listView_Comanda.requestFocus();

                esconderTeclado();
            } else {
                Toast.makeText(getApplicationContext(), "Selecione um item para inserir uma observação", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Comanda vazia", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void salvarComanda(View view) {
        db = dataBase.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        ContentValues contentValues2 = new ContentValues();

        int qtde_item;
        int testeTamanho;
        int nComanda;
        int nMesa;
        int fim = 0;
        String codigo_produto;
        String descricao_produto, obs_item;

        if (conectado(this)) {
            if (!txt_nComanda.getText().toString().isEmpty()) {
                if (!txt_nMesa.getText().toString().isEmpty()) {
                    if (listView_Comanda.getLastVisiblePosition() != -1) {
                        if (produto_Selecionado == null) {
                            nComanda = Integer.parseInt(txt_nComanda.getText().toString());
                            nMesa = Integer.parseInt(txt_nMesa.getText().toString());

                            for (int i = 0; i < x; i++) {
                                if (qtdeI[i] != null) {
                                    testeTamanho = Integer.parseInt(qtdeI[i].substring(0, 1));

                                    if (testeTamanho == 0) {
                                        qtde_item = Integer.parseInt(qtdeI[i].substring(2, 3));
                                        descricao_produto = qtdeI[i].substring(4);
                                    } else {
                                        qtde_item = Integer.parseInt(qtdeI[i].substring(2, 4));
                                        descricao_produto = qtdeI[i].substring(5);
                                    }

                                    String query_Codigo = "SELECT codigo FROM " + dataBase.TABLE_NAME + " WHERE nome = '" + descricao_produto + "'";
                                    exec2(query_Codigo);
                                    codigo_produto = produtos_Comanda.toString().substring(9, produtos_Comanda.toString().length() - 2);

                                    //busca as posições da listView que já foram enviadas para a impressão para não enviar os itens de novo
                                    String query_Posicoes = "SELECT posicao_ocupada FROM " + dataBase.TABLE_NAME3 + " WHERE numero_comanda = '" + nComanda + "' and numer_mesa = '" + nMesa + "'";

                                    Cursor cursor = db.rawQuery(query_Posicoes, null);
                                    cursor.moveToFirst();

                                    for (int j = 0; j < cursor.getCount(); j++) {
                                        String desc = cursor.getString(0);
                                        enviado[j] = desc;
                                        cursor.moveToNext();
                                    }
                                    cursor.close();

                                    int vazio = 0;

                                    for (int y = 0; y <= i; y++) {
                                        if (enviado[y] != null) {
                                            for (int z = 0; z <= i; z++) {
                                                if (enviado[y].equalsIgnoreCase(String.valueOf(posicoes[z]))) {
                                                    vazio = 1;
                                                } else {
                                                    vazio = 0;
                                                }
                                            }
                                        }
                                    }

                                    obs_item = "";

                                    for (int a = 0; a < o; a++) {
                                        if (itensObservacoes[a] != null) {
                                            if (itensObservacoes[a].equalsIgnoreCase(descricao_produto)) {
                                                obs_item = observacoes[a];
                                                //itensObservacoes[a] = null;
                                                //observacoes[a] = null;
                                                //break;
                                            }
                                        }
                                    }

                                    if (vazio == 0) {
                                        try {
                                            //aqui eu salvo as informações na tabela comanda
                                            contentValues.put("numero_comanda", nComanda);
                                            contentValues.put("numero_mesa", nMesa);
                                            contentValues.put("qtde_item", qtde_item);
                                            contentValues.put("descricao_produto", descricao_produto);
                                            contentValues.put("obs_item", obs_item);
                                            contentValues.put("enviado", "Sim");

                                            db.insert(dataBase.TABLE_NAME1, null, contentValues);

                                            //aqui eu salvo as informações na tabela posições
                                            contentValues2.put("numero_comanda", nComanda);
                                            contentValues2.put("numer_mesa", nMesa);
                                            contentValues2.put("posicao_ocupada", posicoes[i]);

                                            db.insert(dataBase.TABLE_NAME3, null, contentValues2);

                                            if (i == x - 1) {
                                                fim = 1;
                                            }

                                            //aqui eu chamo o método que cria, salva e envia o txt para o servidor
                                            salvarTxt(qtde_item, codigo_produto, obs_item, fim, i + 1);

                                            if (i == x - 1) {
                                                //limpo as posições do array
                                                for (int p = 0; p < posicaoArray; p++) {
                                                    produtos_Nao_Enviados[p] = null;
                                                }

                                                //zero a variável posicaoArray para não estourar o array
                                                posicaoArray = 0;

                                                //limpo o array de observações
                                                for (int q = 0; q < o; q++) {
                                                    itensObservacoes[q] = null;
                                                    observacoes[q] = null;
                                                }

                                                o = 0;

                                                alertCarga(this);

                                                //Intent intent = new Intent(this, Main2Activity.class);
                                                //startActivity(intent);

                                                /*File diretorio = new File(caminhoOrigem.toString().substring(0, caminhoOrigem.toString().length() - 13));
                                                if (diretorio.exists()) {
                                                    deletaTxt(caminhoOrigem);
                                                }*/
                                            }
                                        } catch (Exception e) {
                                            Toast.makeText(getApplicationContext(), "Exception: " + e, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Desmarque o item para salvar a comanda", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Comanda vazia", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Preencha o número da mesa", Toast.LENGTH_SHORT).show();
                    txt_nMesa.requestFocus();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Preencha o número da comanda", Toast.LENGTH_SHORT).show();
                txt_nComanda.requestFocus();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Conecte-se à rede: " + buscaNomeRedeMain(), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
       }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void salvarTxt(int qtde_item, String codigo_produto, String obs, int fim, int i) throws IOException {
        //busca a data que está no banco de dados
        String select = "SELECT data, nomeCelular, ip_servidor FROM " + dataBase.TABLE_NAME8;

        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        String data = null;
        String nomeC = null;

        for (int c = 0; c < cursor.getCount(); c++) {
            data = cursor.getString(0);
            nomeC = cursor.getString(1);
            ip = cursor.getString(2);
            cursor.moveToNext();
        }
        cursor.close();

        File diretorio;
        String diretorioApp;

        diretorioApp = Environment.getExternalStoragePublicDirectory(String.valueOf(Environment.getDataDirectory())) + "/" + "comandas";

        diretorio = new File(diretorioApp);
        diretorio.mkdirs();

        caminhoOrigem = diretorio;

        //Quando o File() tem um parâmetro ele cria um diretório.
        //Quando tem dois ele cria um arquivo no diretório onde é informado.
        String nomeArquivo = txt_nComanda.getText().toString();

        final File fileExt = new File(diretorioApp, nomeC + ".txt");

        //Cria o arquivo
        if (!fileExt.exists()){
            fileExt.getParentFile().mkdirs();
            fileExt.createNewFile();
        }

        //Abre o arquivo
        //FileOutputStream fosExt = new FileOutputStream(fileExt);
        FileWriter fw = new FileWriter(fileExt, true);
        BufferedWriter bw = new BufferedWriter(fw);

        int numM  = Integer.parseInt(txt_nMesa.getText().toString());
        long codProd = Integer.parseInt(codigo_produto.toString());
        String nC, nM, qI, cP, zeros = "", zerosItem = "" , numItem = "";

        //Adiciona o 0 de acordo com o número de casas da variável
        int testeNC =  6 - Integer.parseInt(String.valueOf(txt_nComanda.getText().toString().length()));

        for (int t = 0; t < testeNC; t++) {
            zeros = zeros + "0";
        }
        nC = zeros + txt_nComanda.getText().toString();

        int nI =  4 - String.valueOf(i).length();

        for (int t = 0; t < nI; t++) {
            zerosItem = zerosItem + "0";
        }
        numItem = zerosItem + i;

        if (numM >= 10){
            nM = "0000" + Integer.parseInt(txt_nMesa.getText().toString());
        } else {
            nM = "00000" + Integer.parseInt(txt_nMesa.getText().toString());
        }

        if (qtde_item >= 10){
            qI = "0000" + qtde_item;
        } else {
            qI = "00000" + qtde_item;
        }

        if (codProd >= 10){
            cP = "0000" + Integer.parseInt(codigo_produto);
        } else {
            cP = "00000" + Integer.parseInt(codigo_produto);
        }

        if (codProd >= 100){
            cP = "000" + Integer.parseInt(codigo_produto);
        }

        bw.write("CMD" + nC + data + cP + qI + nM + numItem + "celular" + nomeC + "\r\n");

        if (!obs.equalsIgnoreCase("")){
            bw.write(obs + "\r\n");
        }

        if (fim == 1) {
            bw.write("Fim" + "\r\n");
            bw.close();

            if (buscaModoEnvio() == 0) {
                //Log.v("Envio", "FTP");
                if (isOnline(MainActivity.this)) {
                    enviaPedidoFTP(this);
                    //handler.sendEmptyMessage(0);
                } else {
                    Toast.makeText(MainActivity.this, "Por favor, verifique a sua conexão com a internet!", Toast.LENGTH_LONG).show();
                }
            } else {
                //Log.v("Envio", "Socket");
                enviaPedido();
            }
        } else {
            bw.close();
        }
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public void enviaPedidoFTP(final Context context) {
        String endereco = null, usuario = null, senha = null, nomeCelular = null;
        int porta = 0;
        
        String select = "SELECT nomeCelular, endereco_ftp, usuario_ftp, senha_ftp, porta_ftp FROM " + dataBase.TABLE_NAME8;

        Cursor cursor = db.rawQuery(select, null);

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            nomeCelular = cursor.getString(0);
            endereco = cursor.getString(1);
            usuario = cursor.getString(2);
            senha = cursor.getString(3);
            porta = cursor.getInt(4);
            cursor.moveToNext();
        }
        cursor.close();

        final String finalNomeCelular = nomeCelular;
        final String finalEndereco = endereco;
        final String finalUsuario = usuario;
        final String finalSenha = senha;
        final int finalPorta = porta;

        new Thread(new Runnable() {
            public void run () {
                int random = (int) (1 + Math.random() * 1000); //gera um número para concatenar com o nome do arquivo
                boolean status;

                status = myFTPClientFunctions.ftpConnect(finalEndereco, finalUsuario, finalSenha, finalPorta);

                if (status) {
                    //Log.d(TAG, "Connection Success");

                    String caminhoComanda = Environment.getExternalStoragePublicDirectory(String.valueOf(Environment.getDataDirectory())) + "/" + "comandas/" + finalNomeCelular + ".txt";
                    String destComanda = "/c/Note" + finalNomeCelular;
                    String nomeArquivoDeletar = finalNomeCelular + ".txt";
                    String nomeArquivo = finalNomeCelular + "(" + random + ")" + ".txt";

                    Log.d(TAG, nomeArquivo);

                    if (myFTPClientFunctions.ftpUpload(caminhoComanda, nomeArquivo, destComanda, context)) {
                        if (myFTPClientFunctions.ftpDisconnect()) {
                            File diretorio = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(String.valueOf(Environment.getDataDirectory())) + "/" + "comandas"));
                            File caminhoCompleto = new File(Environment.getExternalStoragePublicDirectory(String.valueOf(Environment.getDataDirectory())) + "/" + "comandas" + "/" + nomeArquivoDeletar);

                            if (diretorio.exists()) {
                                deletaTxt(caminhoCompleto);
                            }
                        }
                        //Log.d(TAG, "Disconnected");
                    }
                } else {
                    //Log.d(TAG, "Connection failed");
                    //shandler.sendEmptyMessage(4);
                }
            }
        }).start();
    }

    public void enviaPedido() {
        db = dataBase.getWritableDatabase();

        Thread t = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                ObjectOutputStream out = null;
                BufferedReader reader;
                int quant_linhas = 0;
                int x = 0;
                String nomeFile = null;

                try {
                    Socket socket = new Socket(ip, 5560);

                    File arquivos[];
                    File diretorio = new File(String.valueOf(caminhoOrigem));
                    arquivos = diretorio.listFiles();

                    if (Build.VERSION.SDK_INT < 24) {
                        for (int i = 0; i < arquivos.length; i++) {
                            String nomeArquivo = arquivos[i].getName();
                            nomeFile = arquivos[i].getName();

                            File txt = new File(diretorio + "/" + nomeArquivo);

                            reader = new BufferedReader(new FileReader(txt));

                            while(reader.ready()) {
                                String linha = reader.readLine();

                                if (linha != null) {
                                    if (!linha.equalsIgnoreCase("")) {
                                        quant_linhas = quant_linhas + 1;
                                    }
                                }
                            }
                        }
                    } else {
                        for (int i = 0; i < arquivos.length; i++) {
                            String nomeArquivo = arquivos[i].getName();
                            nomeFile = arquivos[i].getName();

                            File txt = new File(diretorio + "/" + nomeArquivo);

                            reader = new BufferedReader(new FileReader(txt));

                            quant_linhas = quant_linhas + (int) reader.lines().count();
                        }
                    }

                    out = new ObjectOutputStream(socket.getOutputStream());

                    //mando o nome do celular para criar o txt na pasta certa
                    out.writeUTF(nomeFile);

                    String[] arrayLinhas = new String[quant_linhas];

                    for (int l = 0; l < arquivos.length; l++) {
                        String nomeArquivo = arquivos[l].getName();
                        nomeFile = arquivos[l].getName();

                        File txt = new File(diretorio + "/" + nomeArquivo);

                        reader = new BufferedReader(new FileReader(txt));

                        while (reader.ready()) {
                            String linha = reader.readLine();

                            if (linha != null) {
                                if (!linha.equalsIgnoreCase("")) {
                                    arrayLinhas[x] = linha;
                                    x++;
                                }
                            }
                        }
                    }

                    //mando a quantidade de linhas para o for
                    out.writeInt(arrayLinhas.length);

                    for (int i = 0; i < arrayLinhas.length; i++) {
                        if (arrayLinhas[i] != null) {
                            out.writeUTF(arrayLinhas[i].trim());
                        }
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        try {
                            out.close();

                            File diretorio = new File(String.valueOf(caminhoOrigem));
                            File caminhoCompleto = new File(caminhoOrigem + "/" + nomeFile);

                            if (diretorio.exists()) {
                                deletaTxt(caminhoCompleto);
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        t.start();
    }

    public boolean conectado(Context context) {
        boolean conectado = false;

        try{
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if ((cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable() || !cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable()) && cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                String nomeRede = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getExtraInfo();
                if (nomeRede.substring(1, nomeRede.length() - 1).equalsIgnoreCase("METHASYSTEMS") || nomeRede.substring(1, nomeRede.length() - 1).equalsIgnoreCase(buscaNomeRedeMain())) {
                    conectado = true;
                }
            }
        } catch (Exception e) {
            //MensagemToast(e.getMessage(), this);
            return false;
        }
        return conectado;
    }

    public String buscaNomeRedeMain() {
        String rede = null;

        String select = "SELECT nomeRede FROM " + dataBase.TABLE_NAME8;
        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int c = 0; c < cursor.getCount(); c++) {
            rede = cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();

        return rede;
    }

    public int buscaModoEnvio() {
        int tipo = -1;

        String select = "SELECT tipo_envio FROM " + dataBase.TABLE_NAME8;
        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int c = 0; c < cursor.getCount(); c++) {
            tipo = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();

        return tipo;
    }

    public void deletaTxt(File file){
        file.delete();
    }

    private List<Map<String,Object>> exec(String query){
        SQLiteDatabase db = dataBase.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        produtos = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < cursor.getCount(); i++){
            Map<String, Object> item = new HashMap<String, Object>();
            String desc = cursor.getString(0);
            item.put("descricao_produto", desc);
            produtos.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return produtos;
    }

    private List<Map<String,Object>> exec2(String query_Descricao){
        SQLiteDatabase db = dataBase.getReadableDatabase();

        Cursor cursor = db.rawQuery(query_Descricao, null);

        cursor.moveToFirst();

        produtos_Comanda = new ArrayList<Map<String, Object>>();

        for (int i = 0; i < cursor.getCount(); i++){
            Map<String, Object> item = new HashMap<String, Object>();
            String cod = cursor.getString(0);
            item.put("codigo", cod);
            produtos_Comanda.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return produtos_Comanda;
    }

    private List<Map<String,Object>> exec3(String query){
        SQLiteDatabase db = dataBase.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            String desc = cursor.getString(0);
            item.put("qtde_item", qtdeItem);
            item.put("descricao_produto", desc);
            produtos_Descricao.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return produtos_Descricao;
    }

    private List<Map<String,Object>> exec4(String query){
        SQLiteDatabase db = dataBase.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            String desc = cursor.getString(0);
            item.put("descricao_produto", desc);
            teste.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return teste;
    }

    public void buscaPosicao(){
        String select = "SELECT posicao_ocupada FROM " + dataBase.TABLE_NAME3 + " WHERE numero_comanda = '" + txt_nComanda.getText() + "' and" +
                " numer_mesa = '" + txt_nMesa.getText() + "'";

        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++){
            String desc = cursor.getString(0);
            posicoes_ocupadas[i] = desc;
            cursor.moveToNext();
        }
        cursor.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void observacao(final int posicao) {
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o titulo
        builder.setTitle("Observação do item:");

        String produto = produto_Selecionado.substring(19, produto_Selecionado.length() - 14);
        final String mensagem;

        if (produto.substring(produto.length() - 1).equalsIgnoreCase(",")){
            mensagem = produto_Selecionado.substring(19, produto_Selecionado.length() - 15);
        } else {
            mensagem = produto_Selecionado.substring(19, produto_Selecionado.length() - 14);
        }

        //define uma mensagem
        builder.setMessage(mensagem);

        final EditText obs = new EditText(this);
        obs.setSingleLine(false);
        obs.setHint("Observações");
        builder.setView(obs);

        obs.setText(buscaObservacao(posicao));

        //define um botão como positivo
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                esconderTeclado();

                produto_Selecionado = null;

                posicaoAnterior = -1;

                posicaoBloqueada = -1;

                spinner_Grupos.setEnabled(true);

                listView_Produtos.setEnabled(true);

                viewSelecionada.setSelected(false);

                viewSelecionada.setBackgroundColor(Color.TRANSPARENT);

                Toast.makeText(MainActivity.this, "Operação cancelada", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String ob = "";
                String separador = "------------------------------";
                int cont;
                char proxChar = 0;

                for (int v = 0; v < obs.getText().length(); v++) {
                    char carac = obs.getText().charAt(v);

                    cont = v + 1;

                    if (cont < obs.getText().length()) {
                        proxChar = obs.getText().charAt(cont);
                    }

                    if (carac == '\n') {
                        if (carac == '\n' && proxChar == '\n') {
                            ob = ob + "\r\n" + separador;
                        } else {
                            ob = ob + "\r\n";
                        }
                    } else {
                        ob = ob + carac;
                    }
                }

                alteraObs(posicao, ob);

                itensObservacoes[o] = mensagem;

                observacoes[o] = ob;

                o++;

                esconderTeclado();

                produto_Selecionado = null;

                posicaoAnterior = -1;

                posicaoBloqueada = -1;

                spinner_Grupos.setEnabled(true);

                listView_Produtos.setEnabled(true);

                viewSelecionada.setSelected(false);

                viewSelecionada.setBackgroundColor(Color.TRANSPARENT);

                Toast.makeText(MainActivity.this, "Observação salva com sucesso", Toast.LENGTH_SHORT).show();
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);
    }

    public void alteraObs(int posicao, String ob) {
        ContentValues contentValues = new ContentValues();

        if (buscaObs(posicao)) {
            contentValues.put("observacao", ob);

            db.update(dataBase.TABLE_NAME7, contentValues, "num_Comanda = " + Integer.parseInt(txt_nComanda.getText().toString()) + " AND num_Mesa = " + Integer.parseInt(txt_nMesa.getText().toString()) + " AND posicao = " + posicao + " AND descricao_produto = '" + nome + "'", null);
        } else {
            contentValues.put("num_Comanda", Integer.parseInt(txt_nComanda.getText().toString()));
            contentValues.put("num_Mesa", Integer.parseInt(txt_nMesa.getText().toString()));
            contentValues.put("posicao", posicao);
            contentValues.put("observacao", ob);
            contentValues.put("descricao_produto", nome);

            db.insert(dataBase.TABLE_NAME7, null, contentValues);
        }
    }

    public boolean buscaObs(int posicao) {
        boolean retorno;
        String obs = "";

        String select = "SELECT observacao FROM " + dataBase.TABLE_NAME7 + " WHERE num_Comanda = " + Integer.parseInt(txt_nComanda.getText().toString()) + " AND num_Mesa = " + Integer.parseInt(txt_nMesa.getText().toString()) + " AND posicao = " + posicao + " AND descricao_produto = '" + nome + "'";

        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            obs = cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();

        if (obs.equalsIgnoreCase("")) {
            retorno = false;
        } else {
            retorno = true;
        }

        return retorno;
    }

    public String buscaObservacao(int posicao) {
        String observacao = "";

        String select = "SELECT observacao FROM " + dataBase.TABLE_NAME7 + " WHERE num_Comanda = " + Integer.parseInt(txt_nComanda.getText().toString()) + " AND num_Mesa = " + Integer.parseInt(txt_nMesa.getText().toString()) + " AND posicao = " + posicao + " AND descricao_produto = '" + nome + "'";

        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            observacao = cursor.getString(0);
            cursor.moveToNext();
        }
        cursor.close();

        return observacao;
    }

    //valida se o campo do número da comanda está vazio
    public void validaNComanda() {
        if (txt_nComanda.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Preencha o número da comanda", Toast.LENGTH_SHORT).show();
            txt_nComanda.requestFocus();
        } else {
            buscaComanda();
            txt_nComanda.setNextFocusDownId(txt_nMesa.getId());
        }
    }

    //valida se o campo do número da mesa está vazio
    public void validaNMesa() {
        if (txt_nMesa.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Preencha o número da mesa", Toast.LENGTH_SHORT).show();
        } else {
            txt_nMesa.setEnabled(false);
        }
    }

    //fecha o teclado
    public void esconderTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void buscaComanda() {
        String select = "SELECT numero_mesa, qtde_item, descricao_produto FROM " + dataBase.TABLE_NAME1 + " WHERE numero_comanda = " + txt_nComanda.getText().toString();

        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        produtos_Descricao = new ArrayList<>();

        for (int i = 0; i < cursor.getCount(); i++) {
            Map<String, Object> item = new HashMap<String, Object>();

            int nMesa = cursor.getInt(0);
            int quant = cursor.getInt(1);
            String desc = cursor.getString(2);

            txt_nMesa.setText(String.valueOf(nMesa));

            item.put("qtde_item", quant);
            item.put("descricao_produto", desc);

            produtos_Descricao.add(item);

            cursor.moveToNext();
        }
        cursor.close();

        teste = exec4(select);

        String query = "SELECT posicao_ocupada FROM " + dataBase.TABLE_NAME3 + " WHERE numero_comanda = " + txt_nComanda.getText().toString();

        Cursor cursor2 = db.rawQuery(query, null);
        cursor2.moveToFirst();

        posicao_Ocupada = -1;

        for (int i = 0; i < cursor2.getCount(); i++) {
            posicao_Ocupada = cursor2.getInt(0);
            cursor2.moveToNext();
        }
        cursor2.close();

        SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), produtos_Descricao, R.layout.activity_main4, de_Pedidos, para_Pedidos);

        listView_Comanda = findViewById(R.id.listView_Comanda);

        listView_Comanda.setAdapter(simpleAdapter);

        if (posicao_Ocupada == -1) {
            spinner_Grupos.setSelection(0);

            produtos = new ArrayList<>();
            produtos_Comanda = new ArrayList<>();
            SimpleAdapter simpleAdapter2 = new SimpleAdapter(getApplicationContext(), produtos, R.layout.activity_main3, de, para);
            listView_Produtos.setAdapter(simpleAdapter2);

            teste = new ArrayList<>();
            SimpleAdapter simpleAdapter3 = new SimpleAdapter(getApplicationContext(), teste, R.layout.activity_main4, de_Pedidos, para_Pedidos);
            listView_Comanda = findViewById(R.id.listView_Comanda);
            listView_Comanda.setAdapter(simpleAdapter3);

            for (int i = 0; i < posicoes_ocupadas.length; i++) {
                if (posicoes_ocupadas[i] != null) {
                    posicoes_ocupadas[i] = null;
                }
            }

            txt_nMesa.setEnabled(true);

            txt_nMesa.setText("");

            x = 0;
        } else {
            spinner_Grupos.setSelection(0);

            produtos = new ArrayList<>();
            produtos_Comanda = new ArrayList<>();
            SimpleAdapter simpleAdapter2 = new SimpleAdapter(getApplicationContext(), produtos, R.layout.activity_main3, de, para);
            listView_Produtos.setAdapter(simpleAdapter2);

            x = posicao_Ocupada + 1;

            txt_nMesa.setEnabled(false);

            esconderTeclado();
        }
    }

    public void visualizaComanda(View view) {
        if (txt_nComanda.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(getApplicationContext(), "Preencha o número da comanda", Toast.LENGTH_SHORT).show();
        } else if (txt_nMesa.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(getApplicationContext(), "Preencha o número da mesa", Toast.LENGTH_SHORT).show();
        } else if (listView_Comanda.getLastVisiblePosition() == -1) {
            Toast.makeText(getApplicationContext(), "Comanda vazia", Toast.LENGTH_SHORT).show();
        } else {
            String comandaCompleta = buscaComandaCompleta();

            //Cria o gerador do AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            //define o titulo
            builder.setTitle("Comanda: " + txt_nComanda.getText().toString() + "  Mesa: " + txt_nMesa.getText().toString() + "\r\n" + "Itens:");

            builder.setMessage(comandaCompleta);

            builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            //cria o AlertDialog
            alerta = builder.create();

            //Exibe
            alerta.show();

            alerta.setCanceledOnTouchOutside(false);
        }
    }

    public String buscaComandaCompleta() {
        String comandaCompleta = null;
        String qtde;
        String desc;
        String obs;

        String select = "SELECT qtde_item, descricao_produto, obs_item FROM " + dataBase.TABLE_NAME1 + " WHERE numero_comanda = " + txt_nComanda.getText().toString() +
                " AND numero_mesa = " + txt_nMesa.getText().toString();

        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int c = 0; c < cursor.getCount(); c++) {
            qtde = cursor.getString(0);
            desc = cursor.getString(1);
            obs = cursor.getString(2);

            if (comandaCompleta == null) {
                if (obs.equalsIgnoreCase("")) {
                    comandaCompleta = qtde + "  " + desc + "\r\n" + obs + "\r\n";
                } else {
                    comandaCompleta = qtde + "  " + desc + "\r\n" + obs + "\r\n" + "\r\n";
                }
            } else {
                if (obs.equalsIgnoreCase("")) {
                    comandaCompleta = comandaCompleta + qtde + "  " + desc + "\r\n" + obs + "\r\n";
                } else {
                    comandaCompleta = comandaCompleta + qtde + "  " + desc + "\r\n" + obs + "\r\n" + "\r\n";
                }
            }
            cursor.moveToNext();
        }
        cursor.close();

        return comandaCompleta;
    }

    public void alteraMesa(final View view) {
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o titulo
        builder.setTitle("Comanda: " + txt_nComanda.getText().toString() + "  Mesa: " + txt_nMesa.getText().toString());

        final EditText nM = new EditText(this);
        nM.setSingleLine(true);
        nM.setHint("digite o número da mesa");
        nM.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(nM);

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "Operação cancelada", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!nM.getText().toString().equalsIgnoreCase("")) {
                    if (nM.getText().toString().length() > 2) {
                        alteraMesa(view);
                        Toast.makeText(getApplicationContext(), "Número da mesa deve conter 2 dígitos", Toast.LENGTH_SHORT).show();
                    } else {
                        ContentValues contentValues = new ContentValues();
                        ContentValues contentValues1 = new ContentValues();
                        ContentValues contentValues2 = new ContentValues();

                        contentValues.put("numero_mesa", Integer.parseInt(nM.getText().toString()));
                        contentValues1.put("numer_mesa", Integer.parseInt(nM.getText().toString()));
                        contentValues2.put("num_Mesa", Integer.parseInt(nM.getText().toString()));

                        db.update(dataBase.TABLE_NAME1, contentValues, "numero_comanda = " + Integer.parseInt(txt_nComanda.getText().toString()), null);
                        db.update(dataBase.TABLE_NAME3, contentValues1, "numero_comanda = " + Integer.parseInt(txt_nComanda.getText().toString()), null);
                        db.update(dataBase.TABLE_NAME7, contentValues2, "num_Comanda = " + Integer.parseInt(txt_nComanda.getText().toString()), null);

                        txt_nMesa.setText(nM.getText().toString());

                        Toast.makeText(getApplicationContext(), "Número da mesa alterado", Toast.LENGTH_SHORT).show();
                    }
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);
    }

    public void limpaComanda(View view) {
        txt_nComanda.setText("");

        spinner_Grupos.setSelection(0);

        produtos = new ArrayList<>();
        produtos_Comanda = new ArrayList<>();
        SimpleAdapter simpleAdapter2 = new SimpleAdapter(getApplicationContext(), produtos, R.layout.activity_main3, de, para);
        listView_Produtos.setAdapter(simpleAdapter2);

        teste = new ArrayList<>();
        SimpleAdapter simpleAdapter3 = new SimpleAdapter(getApplicationContext(), teste, R.layout.activity_main4, de_Pedidos, para_Pedidos);
        listView_Comanda = findViewById(R.id.listView_Comanda);
        listView_Comanda.setAdapter(simpleAdapter3);

        for (int i = 0; i < posicoes_ocupadas.length; i++) {
            if (posicoes_ocupadas[i] != null) {
                posicoes_ocupadas[i] = null;
            }
        }

        txt_nMesa.setEnabled(true);
        txt_nMesa.setText("");
        txt_nComanda.requestFocus();

        x = 0;
    }

    @Override
    protected void onDestroy(){
        dataBase.close();      //Fecha a conexão com o banco
        super.onDestroy();
    }
}
