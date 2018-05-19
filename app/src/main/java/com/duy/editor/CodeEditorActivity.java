/*
 * Copyright 2018 Mr Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.editor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.duy.ccppcompiler.R;
import com.duy.ccppcompiler.compiler.CompileManager;
import com.duy.ccppcompiler.compiler.CompileTask;
import com.duy.ccppcompiler.compiler.compilers.CompilerFactory;
import com.duy.ccppcompiler.compiler.compilers.INativeCompiler;
import com.duy.ccppcompiler.console.TermActivity;
import com.duy.ccppcompiler.diagnostic.DiagnosticFragment;
import com.duy.ccppcompiler.diagnostic.DiagnosticPresenter;
import com.duy.ide.editor.BaseEditorActivity;
import com.jecelyin.editor.v2.editor.EditorDelegate;
import com.pdaxrom.cctools.BuildConstants;
import com.pdaxrom.packagemanager.Environment;
import com.pdaxrom.packagemanager.PackageManagerActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;

/**
 * Created by Duy on 19-May-18.
 */

public class CodeEditorActivity extends BaseEditorActivity {
    public SlidingUpPanelLayout mSlidingUpPanelLayout;
    private DiagnosticPresenter mDiagnosticPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final View toggleView = findViewById(R.id.btn_toggle_panel);
        mSlidingUpPanelLayout = findViewById(R.id.diagnostic_panel);
        mSlidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.SimplePanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                toggleView.animate().rotation(180 * slideOffset).start();
            }
        });


        FragmentManager fm = getSupportFragmentManager();
        String tag = DiagnosticFragment.class.getSimpleName();
        DiagnosticFragment diagnosticFragment = (DiagnosticFragment) fm.findFragmentByTag(tag);
        if (diagnosticFragment == null) {
            diagnosticFragment = DiagnosticFragment.newInstance();
        }
        fm.beginTransaction().replace(R.id.container_diagnostic_list_view, diagnosticFragment, tag)
                .commit();
        mDiagnosticPresenter = new DiagnosticPresenter(diagnosticFragment, this, mTabManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu container) {
        //add run button
        container.add(0, R.id.action_run, 0, R.string.run)
                .setIcon(R.drawable.ic_play_arrow_white_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(container);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_install_add_on:
                startActivity(new Intent(this, PackageManagerActivity.class));
                return true;

            case R.id.action_open_terminal:
                openTerminal();
                return true;

            case R.id.action_run:
                compileAndRun();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void compileAndRun() {
        saveAll(false);
        EditorDelegate currentEditor = getCurrentEditorDelegate();
        if (currentEditor == null) {
            return;
        }
        File[] srcFiles = new File[1];
        String path = currentEditor.getPath();
        srcFiles[0] = new File(path);

        INativeCompiler compiler = CompilerFactory.makeCompilerForFile(this, srcFiles);
        CompileManager compileManager = new CompileManager(this);
        compileManager.setDiagnosticPresenter(mDiagnosticPresenter);

        if (compiler != null) {
            CompileTask compileTask = new CompileTask(compiler, srcFiles, compileManager);
            compileTask.execute();
        } else {
            Toast.makeText(this, R.string.unknown_filetype, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected String[] getSupportedFileExtensions() {
        String[] defaultExts = super.getSupportedFileExtensions();
        String[] supportedExts = getResources().getStringArray(R.array.file_extensions);
        String[] finalResult = new String[defaultExts.length + supportedExts.length];
        System.arraycopy(supportedExts, 0, finalResult, 0, supportedExts.length);
        System.arraycopy(defaultExts, 0, finalResult, supportedExts.length, defaultExts.length);
        return finalResult;
    }


    private void openTerminal() {
        EditorDelegate currentEditorDelegate = getCurrentEditorDelegate();
        String workDir = null;
        if (currentEditorDelegate != null) {
            workDir = new File(currentEditorDelegate.getPath()).getParent();
        }
        if (workDir == null) {
            workDir = Environment.getHomeDir(this);
        }
        Intent intent = new Intent(this, TermActivity.class);
        intent.putExtra(BuildConstants.EXTRA_FILE_NAME, "-" + Environment.getShell(this));
        intent.putExtra(BuildConstants.EXTRA_WORK_DIR, workDir);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (closeDrawers()) {
            return;
        }
        if (mSlidingUpPanelLayout != null) {
            SlidingUpPanelLayout.PanelState state = mSlidingUpPanelLayout.getPanelState();
            if (state == SlidingUpPanelLayout.PanelState.EXPANDED
                    || state == SlidingUpPanelLayout.PanelState.DRAGGING) {
                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                return;
            }
        }
        super.onBackPressed();

    }
}