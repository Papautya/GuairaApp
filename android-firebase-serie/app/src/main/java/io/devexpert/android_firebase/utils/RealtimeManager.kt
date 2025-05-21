package io.devexpert.android_firebase.utils

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.devexpert.android_firebase.model.Contact
import io.devexpert.android_firebase.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RealtimeManager(context: Context) {
    private val database = FirebaseDatabase.getInstance()
    private val contactsRef: DatabaseReference = database.reference.child("contacts")
    private val productsRef: DatabaseReference = database.reference.child("products")
    private val authManager = AuthManager(context)

    fun addContact(contact: Contact) {
        val key = contactsRef.push().key
        if (key != null) {
            contactsRef.child(key).setValue(contact)
        }
    }

    fun deleteContact(contactId: String) {
        contactsRef.child(contactId).removeValue()
    }

    fun updateContact(contactId: String, updatedContact: Contact) {
        contactsRef.child(contactId).setValue(updatedContact)
    }

    fun getContactsFlow(): Flow<List<Contact>> {
        val idFilter = authManager.getCurrentUser()?.uid
        val flow = callbackFlow {
            val listener = contactsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val contacts = snapshot.children.mapNotNull {  snapshot ->
                        val contact = snapshot.getValue(Contact::class.java)
                        snapshot.key?.let { contact?.copy(key = it) }
                    }
                    trySend(contacts.filter { it.uid == idFilter }).isSuccess
                }
                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })
            awaitClose { contactsRef.removeEventListener(listener) }
        }
        return flow
    }

    fun addProduct(product: Product) {
        val key = productsRef.push().key ?: return
        productsRef.child(key).setValue(product)
    }

    fun deleteProduct(productId: String) {
        productsRef.child(productId).removeValue()
    }

    fun updateProduct(productId: String, updatedProduct: Product) {
        productsRef.child(productId).setValue(updatedProduct)
    }

    fun getProductsFlow(): Flow<List<Product>> = callbackFlow {
        val listener = productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { snap ->
                    snap.getValue(Product::class.java)
                        ?.copy(id = snap.key)
                }
                trySend(list).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { productsRef.removeEventListener(listener) }
    }
}