package com.example.myapplication8.utilities;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Locale;

public class FileChooser
{
    private static final String PARENT_DIR = "..";

    private Context context;
    private ListView list;
    private Dialog dialog;
    private File currentPath;

    // filter on file extension
    private String extension = null;

    public void setExtension( String extension )
    {
        this.extension = (extension == null) ? null : extension.toLowerCase(Locale.getDefault());
    }

    // file selection event handling
    public interface IFileSelectedListener
    {
        void fileSelected( File file );
    }

    public FileChooser setFileListener( IFileSelectedListener fileListener )
    {
        this.fileListener = fileListener;
        return this;
    }

    private IFileSelectedListener fileListener;

    public FileChooser( Context context )
    {
        this.context = context;
        dialog = new Dialog(context);
        list = new ListView(context);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int which, long id )
            {
                String fileChosen = (String) list.getItemAtPosition(which);
                File chosenFile = getChosenFile(fileChosen);
                if( chosenFile.isDirectory() )
                {
                    refresh(chosenFile);
                }
                else
                {
                    if( fileListener != null )
                    {
                        fileListener.fileSelected(chosenFile);
                    }
                    dialog.dismiss();
                }
            }
        });
        dialog.setContentView(list);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        refresh(Environment.getExternalStorageDirectory());
    }

    public void showDialog()
    {
        dialog.show();
    }


    /**
     * Sort, filter and display the files for the given path.
     */
    private void refresh( File path )
    {
        this.currentPath = path;
        if( path.exists() )
        {
            File[] dirs = path.listFiles(new FileFilter()
            {
                @Override
                public boolean accept( File file )
                {
                    return (file.isDirectory() && file.canRead());
                }
            });
            File[] files = path.listFiles(new FileFilter()
            {
                @Override
                public boolean accept( File file )
                {
                    if( !file.isDirectory() )
                    {
                        if( !file.canRead() )
                        {
                            return false;
                        }
                        else if( extension == null )
                        {
                            return true;
                        }
                        else
                        {
                            return file.getName().toLowerCase(Locale.getDefault()).endsWith(extension);
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
            });

            if( files == null )
            {
                dialog.dismiss();
                return;
            }

            // convert to an array
            int i = 0;
            String[] fileList;
            if( path.getParentFile() == null )
            {
                fileList = new String[dirs.length + files.length];
            }
            else
            {
                fileList = new String[dirs.length + files.length + 1];
                fileList[i++] = PARENT_DIR;
            }
            Arrays.sort(dirs);
            Arrays.sort(files);
            for( File dir : dirs )
            {
                fileList[i++] = dir.getName();
            }
            for( File file : files )
            {
                fileList[i++] = file.getName();
            }

            // refresh the user interface
            dialog.setTitle(currentPath.getPath());
            list.setAdapter(new ArrayAdapter(context, android.R.layout.simple_list_item_1, fileList)
            {
                @Override
                public View getView( int pos, View view, ViewGroup parent )
                {
                    view = super.getView(pos, view, parent);
                    ((TextView) view).setSingleLine(true);
                    return view;
                }
            });
        }
    }


    /**
     * Convert a relative filename into an actual File object.
     */
    private File getChosenFile( String fileChosen )
    {
        if( fileChosen.equals(PARENT_DIR) )
        {
            return currentPath.getParentFile();
        }
        else
        {
            return new File(currentPath, fileChosen);
        }
    }
}
