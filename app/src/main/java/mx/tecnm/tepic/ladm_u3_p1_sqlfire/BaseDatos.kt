package mx.tecnm.tepic.ladm_u3_p1_sqlfire

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(
    context: Context?,
    name:String?,
    factory: SQLiteDatabase.CursorFactory?,
    version:Int
): SQLiteOpenHelper(context,name,factory,version) {

    override fun onCreate(p0: SQLiteDatabase) {
        p0.execSQL("CREATE TABLE Apartado(IdApartado INTEGER PRIMARY KEY AUTOINCREMENT,nombreCliente VARCHAR(200),Producto VARCHAR(200),Precio FLOAT)")

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p3: Int) {
    }


}