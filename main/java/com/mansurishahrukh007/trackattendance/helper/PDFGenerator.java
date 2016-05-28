package com.mansurishahrukh007.trackattendance.helper;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class PDFGenerator {
    private SQLiteHandler db;
    private Context context;

    public PDFGenerator(Context context) {
        this.context = context;
        db = new SQLiteHandler(context);
    }

    public String getFormattedAttendance() {
        ArrayList<String[]> attendance = db.getTotalAttendance();
        String[] date = attendance.get(0);
        String[] attended = attendance.get(1);
        String[] total = attendance.get(2);
        String[] added_at = attendance.get(3);
        String[] modified_at = attendance.get(4);

        String s;
        String format = "%1$-5s%2$-12s%3$-10s%4$-7s%5$-27s%6$-24s\n";
        s = String.format(format, "No. ", "Date", "Attended", "Total", "Added At", "Updated At");
        s += "---------------------------------------------------------------------------------------\n";

        for (int i = 0; i < date.length; i++) {
            s += String.format(format, i + 1, date[i], attended[i], total[i], added_at[i], modified_at[i]);
        }
        return s;
    }

    public void createPDF() {
        String FILE = Environment.getExternalStorageDirectory().toString()
                + "/Track Attendance/" + db.getUserDetails().get("name") + ".pdf";
        Document document = new Document(PageSize.A4);

        // Create Directory in External Storage
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Track Attendance");
        myDir.mkdirs();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(FILE));
            document.open();
            addMetaData(document);
            addTitlePage(document);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        document.close();
        Toast.makeText(context, "PDF File is Created. Location : " + FILE, Toast.LENGTH_LONG).show();
    }

    public void addMetaData(Document document) {
        document.addTitle("Attendance");
        document.addSubject("All Attendance");
        document.addKeywords("Personal, Education, attendance, track attendance");
        document.addAuthor(db.getUserDetails().get("name"));
        document.addCreator("Track Attendance");
        document.addCreationDate();
        document.addProducer();
    }

    public void addTitlePage(Document document) throws DocumentException {

        Font titleFont = new Font(Font.FontFamily.COURIER, 40, Font.BOLD, BaseColor.GRAY);
        Font smallBold = new Font(Font.FontFamily.COURIER, 10, Font.NORMAL);
        Font username = new Font(Font.FontFamily.COURIER, 20, Font.BOLD, BaseColor.DARK_GRAY);
        Font useremail = new Font(Font.FontFamily.COURIER, 15, Font.BOLD, BaseColor.GRAY);

        Paragraph prHead = new Paragraph();
        prHead.setFont(titleFont);
        prHead.add("Track Attendance\n\n\n");
        prHead.setAlignment(Element.ALIGN_CENTER);
        document.add(prHead);

        Paragraph userName = new Paragraph();
        userName.setFont(username);
        userName.add(db.getUserDetails().get("name") + "\n");
        userName.setAlignment(Element.ALIGN_LEFT);
        document.add(userName);

        Paragraph userEmail = new Paragraph();
        userEmail.setFont(useremail);
        userEmail.add(db.getUserDetails().get("email") + "\n\n");
        userEmail.setAlignment(Element.ALIGN_LEFT);
        document.add(userEmail);

        //total attendance
        Paragraph attendanceAttended = new Paragraph();
        attendanceAttended.setFont(useremail);
        int a = db.getAttendanceAttended();
        int b = db.getAttendanceTotal();
        String c;
        DecimalFormat df = new DecimalFormat("00.00");
        c = df.format(((float) a / (float) b) * 100) + "%";
        attendanceAttended.add("Total Attendance: " + a + "/" + b + "    " + c + "\n\n");
        attendanceAttended.setAlignment(Element.ALIGN_LEFT);
        document.add(attendanceAttended);


        Paragraph attendance = new Paragraph();
        attendance.setFont(smallBold);
        attendance.add(getFormattedAttendance());
        attendance.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(attendance);

        String s;
        String format = "%1$-5s%2$-12s%3$-10s%4$-7s%5$-27s%6$-24s\n";
        s = "---------------------------------------------------------------------------------------\n";
        s += String.format(format, "    ", "Total", a, b, "    ", "     ");
        Paragraph finalLine = new Paragraph();
        finalLine.setFont(smallBold);
        finalLine.add(s);
        finalLine.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(finalLine);

        document.newPage();
    }
}
