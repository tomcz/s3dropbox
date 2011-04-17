/* ===================================================================================
 * Copyright (c) 2008, Thomas Czarniecki
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of S3DropBox, Thomas Czarniecki, tomczarniecki.com nor
 *    the names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ===================================================================================
 */
package com.tomczarniecki.s3.gui;

import com.tomczarniecki.s3.Lists;
import com.tomczarniecki.s3.S3Bucket;
import com.tomczarniecki.s3.S3Object;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import java.util.List;

class DropBoxTableModel extends AbstractTableModel implements DropBoxModel, ControllerListener {

    public static final String BUCKET_ICON = "folder.gif";
    public static final String OBJECT_ICON = "floppy.gif";

    private final List<DropBoxTableItem> items = Lists.newArrayList();

    private final ImageIcon bucketIcon;
    private final ImageIcon objectIcon;

    public DropBoxTableModel() {
        this.bucketIcon = new ImageIcon(getClass().getResource(BUCKET_ICON));
        this.objectIcon = new ImageIcon(getClass().getResource(OBJECT_ICON));
    }

    public int getRowCount() {
        return items.size();
    }

    public int getColumnCount() {
        return Column.values().length;
    }

    public String getColumnName(int columnIndex) {
        return column(columnIndex).title;
    }

    private Column column(int columnIndex) {
        Column[] columns = Column.values();
        return columns[columnIndex];
    }

    public int findColumn(String columnName) {
        for (Column column : Column.values()) {
            if (column.title.equals(columnName)) {
                return column.ordinal();
            }
        }
        throw new IllegalArgumentException("Unknown column '" + columnName + "'");
    }

    public Class<?> getColumnClass(int columnIndex) {
        return (column(columnIndex) == Column.ICON) ? ImageIcon.class : String.class;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        DropBoxTableItem item = items.get(rowIndex);
        return column(columnIndex).valueAt(item);
    }

    public String getNameAt(int index) {
        return items.get(index).name;
    }

    public List<String> getCurrentNames() {
        List<String> result = Lists.newArrayList();
        for (DropBoxTableItem item : items) {
            if (!Constants.BACK_LINK.equals(item.name)) {
                result.add(item.name);
            }
        }
        return result;
    }

    public void updatedBuckets(List<S3Bucket> buckets) {
        items.clear();
        for (S3Bucket bucket : buckets) {
            DropBoxTableItem item = new DropBoxTableItem();
            item.name = bucket.getName();
            item.icon = bucketIcon;
            items.add(item);
        }
        fireTableDataChanged();
    }

    public void updatedObjects(String bucketName, List<S3Object> objects) {
        items.clear();

        DropBoxTableItem backLink = new DropBoxTableItem();
        backLink.name = Constants.BACK_LINK;
        backLink.icon = bucketIcon;
        items.add(backLink);

        FileSize size = new FileSize();
        for (S3Object object : objects) {
            DropBoxTableItem item = new DropBoxTableItem();
            item.lastModified = object.getLastModified().toString("dd/MM/yyyy HH:mm:ss");
            item.size = size.format(object.getSize());
            item.name = object.getKey();
            item.icon = objectIcon;
            items.add(item);
        }
        fireTableDataChanged();
    }

    enum Column {

        ICON("") {
            @Override
            Object valueAt(DropBoxTableItem item) {
                return item.icon;
            }},

        FILE_NAME("File Name") {
            @Override
            Object valueAt(DropBoxTableItem item) {
                return item.name;
            }},

        SIZE("Size") {
            @Override
            Object valueAt(DropBoxTableItem item) {
                return item.size;
            }},

        LAST_MODIFIED("Last Modified") {
            @Override
            Object valueAt(DropBoxTableItem item) {
                return item.lastModified;
            }};

        final String title;

        private Column(String title) {
            this.title = title;
        }

        abstract Object valueAt(DropBoxTableItem item);
    }
}
