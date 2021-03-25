package com.example.lesson9_homework

import android.Manifest
import android.content.ContentResolver

import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lesson9_homework.databinding.FragmentContactsBinding

const val REQUEST_CODE = 42

class ContactsFragment : Fragment() {
    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private val adapter: RecyclerViewContactsAdapter by lazy { RecyclerViewContactsAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.contactsRv.adapter = adapter
        checkPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContacts()
                } else {
                    AlertDialog.Builder(requireContext()).setTitle("Доступ к контактам")
                        .setMessage("Доступ к контактам не предоставлен!")
                        .setNegativeButton("Закрыть") { dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                }
                return
            }
        }


    }


    private fun getContacts() {
        val contentResolver: ContentResolver = requireContext().contentResolver
        val cursorWithContacts: Cursor? =
            contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            )
        cursorWithContacts?.let {
            val contactsList = mutableListOf<String>()
            for (i in 0..it.count) {
                if (it.moveToPosition(i)) {
                    val name =
                        it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    contactsList.add(i, name)
                }
            }
            adapter.setData(contactsList)
        }
        cursorWithContacts?.close()
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                getContacts()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                AlertDialog.Builder(requireContext()).setTitle("Доступ к контактам")
                    .setMessage("Для вывода списка контактов необходимо предоставить доступ к ним")
                    .setPositiveButton("Предоставить доступ") { _, _ ->
                        requestPermission()
                    }.setNegativeButton("Не надо") { dialog, _ ->
                        dialog.dismiss()
                    }.create().show()
            }
            else -> {
                requestPermission()
            }
        }
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = ContactsFragment()
    }
}