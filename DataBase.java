package com.example.augus.comanda;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by augus on 08/02/2018.
 */

public class DataBase extends SQLiteOpenHelper{

    private static final String DB_NAME = "Pedidos";
    static final String TABLE_NAME = "produtos";
    static final String TABLE_NAME1 = "comanda";
    static final String TABLE_NAME2 = "grupos";
    static final String TABLE_NAME3 = "posicoes";
    static final String TABLE_NAME7 = "observacoes";
    static final String TABLE_NAME8 = "configuracoes";
    private static final int VERSION = 20;

    public DataBase(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "codigo INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "nome TEXT NOT NULL," +
                "descricao_produto TEXT," +
                "grupo_produto TEXT" +
                ")"
        );

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME1 + " (" +
                "codigo INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "numero_comanda INTEGER NOT NULL, " +
                "numero_mesa INTEGER NOT NULL, " +
                "qtde_item INTEGER NOT NULL," +
                "descricao_produto TEXT NOT NULL," +
                "obs_item TEXT, " +
                "enviado TEXT" +
                ")"
        );

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME2 + "(" +
                "codigo INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "nome TEXT NOT NULL" +
                ")"
        );

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME3 + "(" +
                "codigo INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "numero_comanda INTEGER NOT NULL, " +
                "numer_mesa INTEGER NOT NULL, " +
                "posicao_ocupada INTEGER NOT NULL" +
                ")"
        );

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME7 + "(" +
                "codigo INTEGER PRIMARY KEY NOT NULL, " +
                "num_Comanda INTEGER NOT NULL, " +
                "num_Mesa INTEGER NOT NULL, " +
                "posicao INTEGER NOT NULL, " +
                "observacao TEXT, " +
                "descricao_produto TEXT" +
                ")"
        );

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME8 + "(" +
                "codigo INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "data TEXT, " +
                "nomeCelular TEXT," +
                "nomeRede TEXT," +
                "ip_servidor TEXT, " +
                "endereco_ftp TEXT," +
                "usuario_ftp TEXT," +
                "senha_ftp TEXT," +
                "porta_ftp INTEGER," +
                "tipo_envio INTEGER" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        if (oldVersion != newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }

        if (oldVersion != newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME1);
            onCreate(sqLiteDatabase);
        }

        if (oldVersion != newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
            onCreate(sqLiteDatabase);
        }

        if (oldVersion != newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME3);
            onCreate(sqLiteDatabase);
        }

        if (oldVersion != newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME7);
            onCreate(sqLiteDatabase);
        }

        if (oldVersion != newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME8);
            onCreate(sqLiteDatabase);
        }
    }

    public void deletaGrupos(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        sqLiteDatabase.execSQL("DELETE FROM [grupos]");
    }

    public void deletaProdutos(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        sqLiteDatabase.execSQL("DELETE FROM [produtos]");
    }

    public void deletaComanda(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        sqLiteDatabase.execSQL("DELETE FROM [comanda]");
    }

    public void deletaPosicao(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        sqLiteDatabase.execSQL("DELETE FROM [posicoes]");
    }

    public void deletaObservacoes() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        sqLiteDatabase.execSQL("DELETE FROM [observacoes]");
    }

    public void deletaConfiguracoes() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        sqLiteDatabase.execSQL("DELETE FROM [configuracoes]");
    }
}
