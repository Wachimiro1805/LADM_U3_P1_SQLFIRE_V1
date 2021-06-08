package mx.tecnm.tepic.ladm_u3_p1_sqlfire

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //VARIABLES
    var baseRemota = FirebaseFirestore.getInstance()
    var baseSQLite = BaseDatos(this,"Apartado",null,1)
    var listaID = ArrayList<String>()

    var dataFirestore = java.util.ArrayList<String>()
    var listaIDFirebase = java.util.ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            insertar()
        }
        button2.setOnClickListener {
            consulta()
        }
        button3.setOnClickListener {
            sincronizar()
        }
        cargarContactos()
    }

    private fun insertar() {
        try{
            var transaccion = baseSQLite.writableDatabase
            var data = ContentValues()

            data.put("IdApartado",idapartado.text.toString().toInt())
            data.put("nombreCliente",nombrecliente.text.toString())
            data.put("Producto",producto.text.toString())
            data.put("Precio",precio.text.toString().toFloat())

            var respuesta = transaccion.insert("Apartado",null,data)

            if(respuesta == -1L){
                mensaje("ERROR!")
            }else{
                mensaje("EXITO!")
                limpiarCampos()
                cargarContactos()
            }
            transaccion.close()
        }catch (err: SQLiteException){
            mensaje(err.message!!)
        }
    }

    private fun sincronizar() {
        try {
            var Firenombre = ""
            var Fireproducto = ""
            var Fireprecio = 0f

            var seleccion = baseSQLite.readableDatabase
            var SQL = "SELECT * FROM Apartado"

            var cursor = seleccion.rawQuery(SQL, null)

            if (cursor.moveToFirst()) {
                do {
                    Firenombre = cursor.getString(1)
                    Fireproducto = cursor.getString(2)
                    Fireprecio = cursor.getString(3).toFloat()

                    var datosInsertar = hashMapOf(
                            "nombreCliente" to Firenombre,
                            "Producto" to Fireproducto,
                            "Precio" to Fireprecio
                    )
                    //Insertar en Firebase
                    baseRemota.collection("Apartado")
                            .add(datosInsertar)
                            .addOnSuccessListener {
                                alerta("SE INSERTO CORRECTAMENTE EN LA NUBE")
                                limpiarCampos()
                            }
                            .addOnFailureListener {
                                mensaje("ERROR: ${it.message!!}")
                            }
                } while (cursor.moveToNext())
            } else {
                mensaje("NO HAY INFORMACION A SINCRONIZAR")
            }
            eliminarTSQL()
            //sincro = true
            cargarFirestore()
            button2.isEnabled = false
            seleccion.close()
        } catch (err: SQLiteException) {
            mensaje(err.message!!)
        }
    }

    private fun cargarFirestore() {
        baseRemota.collection("Apartado").addSnapshotListener{ querySnapshot, error ->

            if(error != null){
                mensaje(error.message!!)
                return@addSnapshotListener
            }

            dataFirestore.clear()
            listaIDFirebase.clear()

            for (document in querySnapshot!!){
                var cadena = "[${document.getString("nombre")}] / ${document.get("producto")} / ${document.get("precio")}"
                dataFirestore.add(cadena)
                listaIDFirebase.add(document.id.toString())
            }
            listamueblesfire.adapter = ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,dataFirestore)
            listamueblesfire.setOnItemClickListener { parent, view, position, id ->
                dialogElimActualizar(position)
            }
        }
    }
    private fun dialogElimActualizar(position: Int) {
        var idABuscar = listaIDFirebase.get(position)
        AlertDialog.Builder(this).setTitle("ATENCION!!")
                .setMessage("QUE DESEAS HACER CON \n ${dataFirestore.get(position)}?")
                .setPositiveButton("ELIMINAR"){d, i->
                    eliminarFirestore(idABuscar)
                }
                .setNeutralButton("ACTUALIZAR"){d,i->
                    var intent = Intent(this,MainActivity2::class.java)
                    intent.putExtra("idElegido",idABuscar)
                    //intent.putExtra("sincronizar",sincro)
                    startActivity(intent)
                }
                .setNegativeButton("CANCELAR"){d,i->}
                .show()
    }
    private fun eliminarFirestore(idABuscar: String) {
        baseRemota.collection("Apartado")
                .document(idABuscar)
                .delete()
                .addOnFailureListener {
                    mensaje("ERROR! ${it.message!!}")
                }
                .addOnSuccessListener {
                    mensaje("SE ELIMINO CON EXITO")
                }
    }

    private fun eliminarTSQL() {
        try {
            var eliminar = baseSQLite.writableDatabase
            var SQL = "DELETE FROM APARTADO"
            eliminar.execSQL(SQL)
            cargarContactos()
            eliminar.close()
        }catch (err:SQLiteException){
            mensaje(err.message!!)
        }
    }

    private fun consulta() {

        try {
            var transaccion=baseSQLite.readableDatabase
            var idABuscar=idapartado.text.toString()

            var cursor = transaccion.query("Apartado", arrayOf("nombreCliente","Producto","Precio"),"IdApartado=?",
                    arrayOf(idABuscar),null,null,null)

            if(cursor.moveToFirst()){
                textView.setText("Nombre:   "+cursor.getString(0)+"\n"+"Producto:     "+cursor.getString(1)+"\nPrecio:     " +cursor.getString(2))
            }else{
                mensaje("ERROR! No se encontró resultado")
            }
            transaccion.close()
            limpiarCampos()
        }catch (err: SQLiteException) {
            mensaje(err.message!!)
        }
    }

    private fun cargarContactos() {
        try{
            var transaccion = baseSQLite.readableDatabase
            var personas = ArrayList<String>()
            var cursor = transaccion.query("Apartado", arrayOf("*"),null,null,null,null,null )

            if(cursor.moveToFirst()) {
                listaID.clear()
                do {
                    var data = "[" + cursor.getInt(0) + "] - " + cursor.getString(1)
                    personas.add(data)
                    listaID.add(cursor.getInt(0).toString())
                } while (cursor.moveToNext())
            }else {
                personas.add("No HAY DATOS")
            }
            listamuebles.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,personas)

            listamuebles.setOnItemClickListener { adapterView, view, posicionItemSeleccionado, l ->
                var idABorrar = listaID.get(posicionItemSeleccionado)
                AlertDialog.Builder(this)
                        .setMessage("¿Qué deseas hacer con el ID: "+idABorrar+"?")
                        .setTitle("ATENCIÓN")
                        .setPositiveButton("Eliminar"){d,i->
                            eliminar(idABorrar)
                        }
                        .setNeutralButton("Actualizar"){d,i->
                            var intent = Intent(this,MainActivity2::class.java)
                            intent.putExtra("idactualizar",idABorrar)
                            startActivity(intent)
                        }
                        .setNegativeButton("CANCELAR"){d,i->
                            d.dismiss()
                        }
                        .show()
            }
            transaccion.close()
        }catch (err: SQLiteException){
            mensaje(err.message!!)
        }
    }

    private fun eliminar(idABorrar: String) {
        try{
            var transaccion = baseSQLite.writableDatabase
            var resultado = transaccion.delete("Apartado","IdApartado=?", arrayOf(idABorrar))
            if(resultado == 0){
                mensaje("NO SE ENCONTRO EL ID"+idABorrar)
            }else{
                mensaje("SE ELIMINO EL ID"+idABorrar)
            }
            transaccion.close()
            cargarContactos()
        }catch (evt: SQLiteException){
            mensaje(evt.message!!)
        }
    }

    private fun limpiarCampos() {
        idapartado.setText("")
        nombrecliente.setText("")
        producto.setText("")
        precio.setText("")
    }
    fun mensaje(m:String){
        AlertDialog.Builder(this)
                .setTitle("ATENCION")
                .setMessage(m)
                .setPositiveButton("OK"){d,i->}
                .show()
    }
    private fun alerta(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }
}