package com.example.chika

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chika.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Chat : AppCompatActivity() {
    private lateinit var binding:ActivityChatBinding
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageList: ArrayList<Message>
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var mDbReference: DatabaseReference
    private var senderRoom:String?=null
    private var receiverRoom:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbReference = FirebaseDatabase.getInstance().reference

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        val toolbar = binding.materialToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$name";
        messageRecyclerView = binding.chatRecycleView
        messageList = arrayListOf()
        messageAdapter = MessageAdapter(this,messageList)
        messageRecyclerView.layoutManager =LinearLayoutManager(this)
        messageRecyclerView.adapter = messageAdapter

        //logic for recycle view of messages
        mDbReference.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                   for(postSnapshot in snapshot.children){
                       val message = postSnapshot.getValue(Message::class.java)
                       messageList.add(message!!)
                   }

                    messageAdapter.notifyDataSetChanged()
                    if (messageAdapter.itemCount > 0){
                        messageRecyclerView.smoothScrollToPosition(messageAdapter.itemCount-1)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("database error","${error.details}")
                }
            })


        binding.btnSend.setOnClickListener {
            val message = binding.edTxtMessage.text.toString()
            val messageObject = Message(message,senderUid)
            mDbReference.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    mDbReference.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }
            binding.edTxtMessage.setText("")
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}