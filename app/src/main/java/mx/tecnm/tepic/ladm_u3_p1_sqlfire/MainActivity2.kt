package mx.tecnm.tepic.ladm_u3_p1_sqlfire

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {

    var baseRemota = FirebaseFirestore.getInstance()
    var baseSQLite = BaseDatos(this,"Apartado",null,1)
    var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        var extras=intent.extras
        id=extras!!.getString("idactualizar")!!

        try {
            var transaccion=baseSQLite.writableDatabase
            var cursor=transaccion.query("Apartado", arrayOf("nombreCliente","Producto","Precio"),"IdApartado=?",
                arrayOf(id),null,null,null)

            if (cursor.moveToFirst()){
                nombreactualizar.setText(cursor.getString(0))
                productoactualizar.setText(cursor.getString(1))
                precioactualizar.setText(cursor.getFloat(2).toString())
            }else{
                mensaje("ERROR! No se recuperó la información.")
            }

        }catch (err: SQLiteException){
            mensaje(err.message!!)
        }

        button3.setOnClickListener {
            actualizar(id)
        }
        button4.setOnClickListener {
            finish()
        }
    }
    private fun actualizar(id:String) {
        try {
            var transaccion=baseSQLite.writableDatabase
            var valores= ContentValues()

            valores.put("nombreCliente",nombreactualizar.text.toString())
            valores.put("Producto",productoactualizar.text.toString())
            valores.put("Precio",precioactualizar.text.toString().toFloat())

            var resultado=transaccion.update("Apartado",valores,"IdApartado=?", arrayOf(id))

            if (resultado>0){
                mensaje("EXITO!")
                finish()
            }else{
                mensaje("ERROR!")
            }
            transaccion.close()
        }catch (err: SQLiteException){
            mensaje(err.message!!)
        }
    }
    fun mensaje(m:String){
        AlertDialog.Builder(this)
            .setTitle("ATENCION")
            .setMessage(m)
            .setPositiveButton("OK"){d,i->}
            .show()
    }
}