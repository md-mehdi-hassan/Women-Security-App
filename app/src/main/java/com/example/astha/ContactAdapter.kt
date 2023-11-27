package com.example.astha

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import java.util.*
internal class ContactAdapter(private val context: Context, listContacts: ArrayList<Contacts>) :
    RecyclerView.Adapter<ContactViewHolder>(), Filterable {
    private var listContacts: ArrayList<Contacts>
    private val mArrayList: ArrayList<Contacts>
    private val mDatabase: SqliteDatabase
    init {
        this.listContacts = listContacts
        this.mArrayList = listContacts
        mDatabase = SqliteDatabase(context)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.contact_list_layout, parent, false)
        return ContactViewHolder(view)
    }
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contacts = listContacts[position]
        holder.tvName.text = contacts.name
        holder.tvPhoneNum.text = contacts.phno
        holder.editContact.setOnClickListener { editTaskDialog(contacts) }
        holder.deleteContact.setOnClickListener {
            mDatabase.deleteContact(contacts.id)
            (context as Activity).finish()
            context.startActivity(context.intent)
        }
    }
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                listContacts = if (charString.isEmpty()) {
                    mArrayList
                }
                else {
                    val filteredList = ArrayList<Contacts>()
                    for (contacts in mArrayList) {
                        if (contacts.name.toLowerCase().contains(charString)) {
                            filteredList.add(contacts)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = listContacts
                return filterResults
            }
            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            )
            {
                listContacts =
                    filterResults.values as ArrayList<Contacts>
                notifyDataSetChanged()
            }
        }
    }
    override fun getItemCount(): Int {
        return listContacts.size
    }
    private fun editTaskDialog(contacts: Contacts) {
        val inflater = LayoutInflater.from(context)
        val subView = inflater.inflate(R.layout.add_contacts, null)
        val nameField: EditText = subView.findViewById(R.id.enterName)
        val contactField: EditText = subView.findViewById(R.id.enterPhoneNum)
        nameField.setText(contacts.name)
        contactField.setText(contacts.phno)
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Edit contact")
        builder.setView(subView)
        builder.create()
        builder.setPositiveButton(
            "EDIT CONTACT"
        ) { _, _ ->
            val name = nameField.text.toString()
            val phNo = contactField.text.toString()
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(
                    context,
                    "Something went wrong. Check your input values",
                    Toast.LENGTH_LONG
                ).show()
            }
            else {
                mDatabase.updateContacts(
                    Contacts(
                        Objects.requireNonNull<Any>(contacts.id) as Int,
                        name,
                        phNo
                    )
                )
                (context as Activity).finish()
                context.startActivity(context.intent)
            }
        }
        builder.setNegativeButton(
            "CANCEL"
        ) { _, _ -> Toast.makeText(context, "Task cancelled", Toast.LENGTH_LONG).show() }
        builder.show()
    }
}