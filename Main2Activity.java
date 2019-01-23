package com.example.augus.comanda;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {
    private DataBase dataBase;
    private SQLiteDatabase db;
    private AlertDialog alerta;
    private ProgressDialog progressBarDialog;
    private ListView listView_ComandasFeitas;
    private List<Map<String,Object>> comandasFeitas;

    ProgressDialog progress;
    Thread t;

    String ip;
    String nomeCelular;
    String data;
    String cmdCompleta;

    String [] de = {"comanda", "mesa"};
    int [] para = {R.id.textView_nComanda, R.id.textView_nMesa};

    String [] de_sem_comanda = {"semComanda"};
    int [] para_sem_comanda = {R.id.textView_mensagem};

    private static final String TAG = "MyFTPClientFunctions";
    MyFTPClientFunctions myFTPClientFunctions = null;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (progressBarDialog != null && progressBarDialog.isShowing()) {
                progressBarDialog.dismiss();
                buscaComandasFeitas();
            }

            if (msg.what == 0) {
                progressBarFtp();
            } else if (msg.what == 1) {
                mensagem();
            } else if (msg.what == 2) {
                Toast.makeText(getApplicationContext(), "Erro ao atualizar!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        dataBase = new DataBase(this);
        db = dataBase.getWritableDatabase();
        //dataBase.deletaConfiguracoes();

        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        final String dateString = sdf.format(date);

        String select = "SELECT data FROM " + dataBase.TABLE_NAME8 + " WHERE codigo = 1";

        Cursor cursor1 = db.rawQuery(select, null);
        cursor1.moveToFirst();

        String data = null;

        for (int c = 0; c < cursor1.getCount(); c++) {
            String d = cursor1.getString(0);
            data = d;
            cursor1.moveToNext();
        }
        cursor1.close();

        if (data != null) {
            if (!data.equalsIgnoreCase(dateString)) {
                alterarData();
            }
        }

        listView_ComandasFeitas = initListView();

        comandasFeitas = new ArrayList<>();

        buscaComandasFeitas();

        FloatingActionButton fab = findViewById(R.id.fab_comanda);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abreComanda(view);
            }
        });

        myFTPClientFunctions = new MyFTPClientFunctions();
    }

    @Override
    protected void onResume(){
        super.onResume();
        buscaComandasFeitas();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int i = item.getItemId();

            //alteraIp();
            //buscaCelular();
            //alteraDataTrabalho();

        if (i == R.id.atualiza){
            carregarTabelas();
            return true;
        } else if (i == R.id.configuracoes){
            //this.finish();

            Intent intent = new Intent(this, Main6Activity.class);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    public boolean Conectado(Context context) {
        boolean conectado = false;

        try{
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if ((cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable() || !cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable()) && cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                String nomeRede = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getExtraInfo();
                if (nomeRede.substring(1, nomeRede.length() - 1).equalsIgnoreCase("METHASYSTEMS") || nomeRede.substring(1, nomeRede.length() - 1).equalsIgnoreCase(buscaNomeRede())) {
                    conectado = true;
                }
            }
        } catch (Exception e) {
            //MensagemToast(e.getMessage(), this);
            return false;
        }
        return conectado;
    }

    public static void MensagemToast(String msg, Activity Atividade) {
        Toast.makeText(Atividade, msg, Toast.LENGTH_SHORT).show();
    }

    public void abreComanda(View view) {
        //if (Conectado(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("abrir", 0);
            startActivity(intent);
        //} else {
            //mensagemErroConexao();
        //}
    }

    public void carregarTabelas() {
        if (Conectado(this)) {
            alertConfirmacao();
        } else {
            mensagemErroConexao();
        }
    }

    public void mensagem(){
        Toast.makeText(getApplicationContext(), "Importação concluída com sucesso!", Toast.LENGTH_SHORT).show();
    }

    public void mensagemErroConexao() {
        Toast.makeText(getApplicationContext(), "Conecte-se à rede: " + buscaNomeRede(), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    public void alertCarga() {
        progress = new ProgressDialog(this);

        progress.setMessage("Atualizando...");

        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        progress.show();

        progress.setCanceledOnTouchOutside(false);

        Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                progress.cancel();
                buscaComandasFeitas();
                mensagem();
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 20000);
    }

    public void progressBarFtp() {
        progressBarDialog = new ProgressDialog(this);

        progressBarDialog.setMessage("Atualizando...");

        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        progressBarDialog.show();

        progressBarDialog.setCanceledOnTouchOutside(false);

        /*Runnable progressRunnable = new Runnable() {

            @Override
            public void run() {
                progressBarDialog.cancel();

                handler.sendEmptyMessage(7);
            }
        };

        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 20000);*/
    }

    public void alertConfirmacao() {
        buscaData();

        buscaIP();

        dataBase = new DataBase(this);

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o titulo
        builder.setTitle("Essa pode ser uma operação demorada. Deseja continuar?");

        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(Main2Activity.this, "Operação cancelada", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db = dataBase.getWritableDatabase();

                if (temComanda()) {
                    apagaComandas();
                } else {
                    dataBase.deletaGrupos();
                    dataBase.deletaProdutos();

                    testaModoEnvio();
                }
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);
    }

    public void apagaComandas() {
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o titulo
        builder.setTitle("Deseja apagar as comandas do celular?");

        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                dataBase.deletaGrupos();
                dataBase.deletaProdutos();

                testaModoEnvio();
            }
        });

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dataBase.deletaGrupos();
                dataBase.deletaProdutos();
                dataBase.deletaComanda();
                dataBase.deletaPosicao();
                dataBase.deletaObservacoes();

                testaModoEnvio();
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);
    }

    public boolean temComanda() {
        boolean achou = false;

        String select = "SELECT COUNT(numero_comanda) FROM " + dataBase.TABLE_NAME1;

        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int x = 0; x < cursor.getCount(); x++) {
            if (cursor.getInt(0) > 0) {
                achou = true;
            }
            cursor.moveToNext();
        }
        cursor.close();

        return achou;
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

    public void testaModoEnvio() {
        if (buscaModoEnvio() == 0) {
            if (isOnline(Main2Activity.this)) {
                atualizaEstoqueFTP();
                handler.sendEmptyMessage(0);
            } else {
                Toast.makeText(Main2Activity.this, "Por favor, verifique a sua conexão com a internet!", Toast.LENGTH_LONG).show();
            }
        } else {
            atualizaEstoque();
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

    public void atualizaEstoqueFTP() {
        String endereco = null, usuario = null, senha = null;
        int porta = 0;

        String select = "SELECT endereco_ftp, usuario_ftp, senha_ftp, porta_ftp FROM " + dataBase.TABLE_NAME8;

        Cursor cursor = db.rawQuery(select, null);

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            endereco = cursor.getString(0);
            usuario = cursor.getString(1);
            senha = cursor.getString(2);
            porta = cursor.getInt(3);
            cursor.moveToNext();
        }
        cursor.close();

        final String finalEndereco = endereco;
        final String finalUsuario = usuario;
        final String finalSenha = senha;
        final int finalPorta = porta;

        new Thread(new Runnable() {
            public void run() {
                boolean status;
                String dest = Environment.getExternalStoragePublicDirectory(String.valueOf(Environment.getDataDirectory())) + "/" + "estoque";
                String src = "/c/";

                File diretorio = new File(dest);
                diretorio.mkdirs();

                status = myFTPClientFunctions.ftpConnect(finalEndereco, finalUsuario, finalSenha, finalPorta);

                if (status) {
                    Log.d(TAG, "Connection Success");

                    String[] list = myFTPClientFunctions.ftpPrintFilesList(src);

                    //chama o método que faz o download do arquivo
                    for (int i = 0; i < list.length; i++) {
                        if (list[i] != null) {
                            if (list[i].equalsIgnoreCase("GRUPOS.txt") || list[i].equalsIgnoreCase("PRODUTOS.txt")) {
                                myFTPClientFunctions.ftpDownload(src + list[i], dest + "/" + list[i]);
                            }
                        }
                    }

                    //chama o método que fecha a conexão com o ftp
                    if (myFTPClientFunctions.ftpDisconnect()){
                        Log.d(TAG, "Disconnected");
                        //handler.sendEmptyMessage(3);

                        //chama o método que lê os arquivos e salva no banco
                        salvaEstoqueFTP(dest);
                    } else {
                        //handler.sendEmptyMessage(5);
                    }
                } else {
                    Log.d(TAG, "Connection failed");
                    handler.sendEmptyMessage(2);
                }
            }
        }).start();
    }

    public void salvaEstoqueFTP(String caminho) {
        BufferedReader reader = null;
        int quant_linhas = 0;
        int x = 0;

        File arquivos[];
        File diretorio = new File(caminho);
        arquivos = diretorio.listFiles();

        try {
            if (arquivos.length > 0) {
                if (Build.VERSION.SDK_INT < 24) {
                    for (int i = 0; i < arquivos.length; i++) {
                        String nomeArquivo = arquivos[i].getName();

                        if (nomeArquivo.equalsIgnoreCase("PRODUTOS.txt")) {
                            File txt = new File(diretorio + "/" + nomeArquivo);

                            reader = new BufferedReader(new FileReader(txt));

                            while (reader.ready()) {
                                String linha = reader.readLine();
                                if (linha != null) {
                                    if (!linha.equalsIgnoreCase("")) {
                                        quant_linhas += quant_linhas + 1;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < arquivos.length; i++) {
                        String nomeArquivo = arquivos[i].getName();

                        if (nomeArquivo.equalsIgnoreCase("PRODUTOS.txt")) {
                            File txt = new File(diretorio + "/" + nomeArquivo);

                            reader = new BufferedReader(new FileReader(txt));
                            quant_linhas = quant_linhas + (int) reader.lines().count();
                        }
                    }
                }

                for(int l = 0; l < arquivos.length; l++) {
                    ContentValues contentValues = new ContentValues();

                    String nomeArquivo = arquivos[l].getName();
                    File txt = new File(diretorio + "/" + nomeArquivo);

                    reader = new BufferedReader(new FileReader(txt));

                    if (nomeArquivo.equalsIgnoreCase("GRUPOS.txt")) {
                        while (reader.ready()) {
                            String linha = reader.readLine();

                            if (!linha.equalsIgnoreCase("")) {
                                int codigo = Integer.parseInt(linha.substring(0, 7));
                                String nomeGrupo = linha.substring(7, linha.length()).trim();

                                contentValues.put("codigo", codigo);
                                contentValues.put("nome", nomeGrupo);

                                db.insert(dataBase.TABLE_NAME2, null, contentValues);
                            }
                        }
                    } else if (nomeArquivo.equalsIgnoreCase("PRODUTOS.txt")) {
                        String[] arrayProdutos = new String[quant_linhas];
                        
                        while (reader.ready()) {
                            String linha = reader.readLine();

                            int achou = 0;

                            if (!linha.equalsIgnoreCase("")) {
                                for (int i = 0; i < x; i++) {
                                    if (arrayProdutos[i] != null) {
                                        if (arrayProdutos[i].equals(linha.substring(14, linha.length()))) {
                                            achou = 1;
                                            break;
                                        }
                                    }
                                }

                                if (achou == 0) {
                                    int codigo = Integer.parseInt(linha.substring(0, 7));
                                    String codigo_grupo = linha.substring(8, 14);
                                    String nome_produto = linha.substring(14, linha.length()).trim();

                                    arrayProdutos[x] = nome_produto;

                                    x++;

                                    contentValues.put("codigo", codigo);
                                    contentValues.put("nome", nome_produto);
                                    contentValues.put("descricao_produto", nome_produto);
                                    contentValues.put("grupo_produto", codigo_grupo);

                                    db.insert(dataBase.TABLE_NAME, null, contentValues);
                                }
                            }
                        }
                    }
                    reader.close();
                }

                deletaPedidosFtp(caminho);
            } else {
                //handler.sendEmptyMessage(6);
                Log.d(TAG, "Pasta vazia");
            }
        } catch (IOException ex) {

        }
    }

    public void deletaPedidosFtp(String src) {
        File caminhoP = new File(src);

        if (caminhoP.isDirectory()) {
            File[] sun = caminhoP.listFiles();
            for (File toDelete : sun) {
                toDelete.delete();
            }
        }

        handler.sendEmptyMessage(1);
    }

    public void atualizaEstoque() {
        new Thread(new Runnable() {
            int codigo_grupo = 0;
            String nome_grupo = "";

            int codigo_produto = 0;
            String nome_produto = "";
            String grupo_produto = "";

            @Override
            public void run() {
                try {
                    //Importação dos grupos
                    Socket socket = new Socket(ip, 1235);

                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                    out.writeUTF("Grupos.txt");

                    out.flush();

                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                    int numGrupos = ois.readInt();

                    ContentValues contentValues = new ContentValues();

                    for (int i = 0; i < numGrupos; i++) {
                        codigo_grupo = ois.readInt();
                        nome_grupo = ois.readUTF();

                        contentValues.put("codigo", codigo_grupo);
                        contentValues.put("nome", nome_grupo);

                        db.insert(dataBase.TABLE_NAME2, null, contentValues);
                    }

                    //Importação dos produtos
                    out = new ObjectOutputStream(socket.getOutputStream());

                    out.writeUTF("Produtos.txt");

                    out.flush();

                    ois = new ObjectInputStream(socket.getInputStream());

                    int numProdutos = ois.readInt();

                    contentValues = new ContentValues();

                    for (int i = 0; i < numProdutos; i++) {
                        codigo_produto = ois.readInt();
                        grupo_produto = String.valueOf(ois.readInt());
                        nome_produto = ois.readUTF();

                        contentValues.put("codigo", codigo_produto);
                        contentValues.put("nome", nome_produto);
                        contentValues.put("descricao_produto", nome_produto);
                        contentValues.put("grupo_produto", grupo_produto);

                        db.insert(dataBase.TABLE_NAME, null, contentValues);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        alertCarga();

        /*Thread t2 = new Thread(new Runnable() {
            int codigo_produto = 0;
            String nome_produto = "";
            String grupo_produto = "";

            @Override
            public void run() {
                try {
                    Socket socket = new Socket(ip, 2346);

                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                    out.writeUTF("Produtos.txt");

                    out.flush();

                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                    int numProdutos = ois.readInt();

                    ContentValues contentValues = new ContentValues();

                    for (int i = 0; i < numProdutos; i++) {
                        codigo_produto = ois.readInt();
                        grupo_produto = String.valueOf(ois.readInt());
                        nome_produto = ois.readUTF();

                        contentValues.put("codigo", codigo_produto);
                        contentValues.put("nome", nome_produto);
                        contentValues.put("descricao_produto", nome_produto);
                        contentValues.put("grupo_produto", grupo_produto);

                        db.insert(dataBase.TABLE_NAME, null, contentValues);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t2.start();*/
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void alterarData() {
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        final String dateString = sdf.format(date);

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o titulo
        builder.setTitle("Atualizar data de trabalho para " + dateString + "?");

        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(Main2Activity.this, "Data não atualizada", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String codigo = null;
                String select = "SELECT codigo FROM " + dataBase.TABLE_NAME8;

                ContentValues contentValues = new ContentValues();

                Cursor cursor = db.rawQuery(select, null);
                cursor.moveToFirst();

                for (int c = 0; c < cursor.getCount(); c++) {
                    String cod = String.valueOf(cursor.getInt(0));
                    codigo = cod;
                    cursor.moveToNext();
                }
                cursor.close();

                contentValues.put("data", dateString);

                db.update(dataBase.TABLE_NAME8, contentValues, "codigo = 1", null);

                Toast.makeText(Main2Activity.this, "Data atualizada", Toast.LENGTH_SHORT).show();
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);
    }

   /* public void nomeCelular(){
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o titulo
        builder.setTitle("Digite o número do celular:");

        final EditText nome = new EditText(this);
        nome.setSingleLine(true);
        builder.setView(nome);

        builder.setNegativeButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ContentValues contentValues = new ContentValues();

                contentValues.put("nomeCelular", nome.getText().toString());

                db.insert(dataBase.TABLE_NAME8, null, contentValues);
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);

        //força o teclado a aparecer
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }*/

    /*private void alteraIp() {
        buscaIP();

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o titulo
        builder.setTitle("IP atual: " + ip);

        builder.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(Main2Activity.this, "Operação cancelada", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Alterar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                novoIP();
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);
    }*/

    /*private void novoIP() {
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o título
        builder.setTitle("Insira o novo IP:");

        final EditText ipS = new EditText(this);
        ipS.setSingleLine(true);
        builder.setView(ipS);

        builder.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(Main2Activity.this, "Operação cancelada", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (ipS.getText().toString().equalsIgnoreCase("")) {

                } else {
                    String ipServidor = null;
                    String select = "SELECT ip_servidor FROM " + dataBase.TABLE_NAME8;

                    ContentValues contentValues = new ContentValues();

                    Cursor cursor = db.rawQuery(select, null);
                    cursor.moveToFirst();

                    for (int c = 0; c < cursor.getCount(); c++) {
                        String cod = String.valueOf(cursor.getString(0));
                        ipServidor = cod;
                        cursor.moveToNext();
                    }
                    cursor.close();

                    if (ipServidor == null) {
                        contentValues.put("codigo", 1);
                        contentValues.put("ip_servidor", ipS.getText().toString());

                        db.insert(dataBase.TABLE_NAME8, null, contentValues);
                    } else {
                        contentValues.put("codigo", 1);
                        contentValues.put("ip_servidor", ipS.getText().toString());

                        db.update(dataBase.TABLE_NAME8, contentValues, null, null);
                    }
                    Toast.makeText(Main2Activity.this, "IP alterado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);
    }*/

    /*private void buscaCelular() {
        buscaNomeCelular();

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o título
        builder.setTitle("Número do celular: " + nomeCelular);

        builder.setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);
    }*/

    /*private void alteraDataTrabalho() {
        buscaData();

        //Cria o gerador do AlertDialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o titulo
        builder.setTitle("Data atual: " + data);

        builder.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(Main2Activity.this, "Operação cancelada", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Alterar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dataNova();
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);
    }*/

    /*private void dataNova() {
        //Cria o gerador do AlertDialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o titulo
        builder.setTitle("Insira a nova data:");

        final EditText dataTrab = new EditText(this);
        dataTrab.addTextChangedListener(Mask.insert("##/##/####", dataTrab)); //máscara para data no editText
        dataTrab.setInputType(InputType.TYPE_CLASS_NUMBER);
        dataTrab.setSingleLine(true);
        builder.setView(dataTrab);

        builder.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(Main2Activity.this, "Operação cancelada", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dataTrab.getText().toString().equalsIgnoreCase("")) {

                } else {
                    ContentValues contentValues = new ContentValues();

                    contentValues.put("codigo", 1);
                    contentValues.put("data", dataTrab.getText().toString());

                    db.update(dataBase.TABLE_NAME8, contentValues, null, null);

                    Toast.makeText(Main2Activity.this, "Data alterada", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        alerta.setCanceledOnTouchOutside(false);
    }*/

    public void buscaIP() {
        String select = "SELECT ip_servidor FROM " + dataBase.TABLE_NAME8;
        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int c = 0; c < cursor.getCount(); c++) {
            String cod = String.valueOf(cursor.getString(0));
            ip = cod;
            cursor.moveToNext();
        }
        cursor.close();
    }

    /*public void buscaNomeCelular() {
        String select = "SELECT nomeCelular FROM " + dataBase.TABLE_NAME8;
        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int c = 0; c < cursor.getCount(); c++) {
            String cel = cursor.getString(0);
            nomeCelular = cel;
            cursor.moveToNext();
        }
        cursor.close();
    }*/

    public void buscaData() {
        String select = "SELECT data FROM " + dataBase.TABLE_NAME8;
        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        for (int c = 0; c < cursor.getCount(); c++) {
            String date = cursor.getString(0);
            data = date;
            cursor.moveToNext();
        }
        cursor.close();
    }

    public String buscaNomeRede() {
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

    private ListView initListView(){
        final ListView list = findViewById(R.id.listView_ComandasProntas);

        list.setClickable(true);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                buscaComanda(adapterView, i);
            }
        });

        list.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch (View v, MotionEvent motionEvent){
                if (listView_ComandasFeitas.getLastVisiblePosition() == -1) {
                    listView_ComandasFeitas.requestDisallowInterceptTouchEvent(false);
                } else {
                    listView_ComandasFeitas.requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });
        return list;
    }

    public void buscaComanda(AdapterView adapterView, int posicao) {
        String comandaCompleta = null;
        String qtde;
        String desc;
        String obs;

        StringBuilder comanda = new StringBuilder();
        StringBuilder mesa = new StringBuilder();

        String info = adapterView.getItemAtPosition(posicao).toString().replaceAll("[abcdefghijklmnopqrstuvwxyz={}º' ':]", "").trim();
        //Log.v("ADAPTERVIEW", adapterView.getItemAtPosition(posicao).toString());
        //Log.v("ADAPTERVIEW", String.valueOf(info.length()));

        if (!info.equalsIgnoreCase("CN")) {
            for (int v = 0; v < info.length(); v++) {
                char carac = info.charAt(v);

                if (carac == 'M') {
                    for (int x = v + 1; x < info.length(); x++) {
                        if (info.charAt(x) == ',') {
                            break;
                        } else {
                            mesa.append(info.charAt(x));
                        }
                    }
                    //Log.v("ADAPTERVIEW", "Mesa" + " - " + mesa);
                } else if (carac == 'N') {
                    for (int x = v + 1; x < info.length(); x++) {
                        if (info.charAt(x) == ',') {
                            break;
                        } else {
                            comanda.append(info.charAt(x));
                        }
                    }
                    //Log.v("ADAPTERVIEW", "Comanda" + " - " + comanda);
                }
            }

            String select = "SELECT qtde_item, descricao_produto, obs_item FROM " + dataBase.TABLE_NAME1 + " WHERE numero_comanda = " + comanda +
                    " AND numero_mesa = " + mesa;

            Cursor cursor = db.rawQuery(select, null);
            cursor.moveToFirst();

            for (int z = 0; z < cursor.getCount(); z++) {
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

            cmdCompleta = comandaCompleta;

            visualizaComanda(comanda, mesa);
        }
    }

    public void visualizaComanda(final StringBuilder comanda, StringBuilder mesa) {
        final Intent intent = new Intent(this, MainActivity.class);

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //define o titulo
        builder.setTitle("Comanda: " + comanda + "  Mesa: " + mesa);

        builder.setMessage(cmdCompleta);

        builder.setPositiveButton("Abrir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                intent.putExtra("abrir", 1);
                intent.putExtra("comanda", String.valueOf(comanda));
                startActivity(intent);
            }
        });

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

    public void buscaComandasFeitas() {
        String select = "SELECT numero_comanda, numero_mesa FROM " + dataBase.TABLE_NAME1 + " GROUP BY numero_comanda";

        Cursor cursor = db.rawQuery(select, null);
        cursor.moveToFirst();

        int contador = cursor.getCount();

        comandasFeitas = new ArrayList<>();

        for (int i = 0; i < cursor.getCount(); i++) {
            Map<String, Object> item = new HashMap<String, Object>();

            int comanda = cursor.getInt(0);
            int mesa = cursor.getInt(1);

            item.put("comanda", "Nº: " + String.valueOf(comanda));
            item.put("mesa", "Mesa: " + String.valueOf(mesa));

            comandasFeitas.add(item);

            cursor.moveToNext();
        }
        cursor.close();

        if (contador <= 0) {
            Map<String, Object> item = new HashMap<String, Object>();

            item.put("semComanda", "Nenhuma comanda salva");

            comandasFeitas.add(item);

            SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), comandasFeitas, R.layout.sem_comandas, de_sem_comanda, para_sem_comanda);

            listView_ComandasFeitas = findViewById(R.id.listView_ComandasProntas);

            listView_ComandasFeitas.setAdapter(simpleAdapter);

            listView_ComandasFeitas.setEnabled(false);

        } else {
            SimpleAdapter simpleAdapter = new SimpleAdapter(getApplicationContext(), comandasFeitas, R.layout.activity_main5, de, para);

            listView_ComandasFeitas = findViewById(R.id.listView_ComandasProntas);

            listView_ComandasFeitas.setAdapter(simpleAdapter);

            listView_ComandasFeitas.setEnabled(true);
        }
    }
}
