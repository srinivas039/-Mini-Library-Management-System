import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

class Book {
    String isbn;
    String title;
    String author;
    int copies;
    Book(String isbn,String title,String author,int copies){this.isbn=isbn;this.title=title;this.author=author;this.copies=copies;}
    String toCsv(){return String.join(",",escape(isbn),escape(title),escape(author),String.valueOf(copies));}
    static Book fromCsv(String line){String[] f=parse(line,4);return new Book(f[0],f[1],f[2],Integer.parseInt(f[3]));}
    static String escape(String s){if(s.contains(",")||s.contains("\"")) return "\"" + s.replace("\"","\"\"") + "\""; return s;}
    static String[] parse(String line,int expected){List<String> out=new ArrayList<>();StringBuilder cur=new StringBuilder();boolean inq=false;for(int i=0;i<line.length();i++){char c=line.charAt(i);if(c=='\"'){if(inq && i+1<line.length() && line.charAt(i+1)=='\"'){cur.append('\"');i++;}else inq=!inq; } else if(c==',' && !inq){out.add(cur.toString());cur.setLength(0);} else cur.append(c);} out.add(cur.toString()); while(out.size()<expected) out.add(""); return out.toArray(new String[0]);}
}

class Member {
    String id;
    String name;
    String phone;
    Member(String id,String name,String phone){this.id=id;this.name=name;this.phone=phone;}
    String toCsv(){return String.join(",",escape(id),escape(name),escape(phone));}
    static Member fromCsv(String line){String[] f=Book.parse(line,3);return new Member(f[0],f[1],f[2]);}
    static String escape(String s){if(s.contains(",")||s.contains("\"")) return "\"" + s.replace("\"","\"\"") + "\""; return s;}
}

class Transaction {
    String txId;
    String isbn;
    String memberId;
    String issueDate;
    String returnDate;
    boolean returned;
    Transaction(String txId,String isbn,String memberId,String issueDate){this.txId=txId;this.isbn=isbn;this.memberId=memberId;this.issueDate=issueDate;this.returnDate="";this.returned=false;}
    Transaction(String txId,String isbn,String memberId,String issueDate,String returnDate,boolean returned){this.txId=txId;this.isbn=isbn;this.memberId=memberId;this.issueDate=issueDate;this.returnDate=returnDate;this.returned=returned;}
    String toCsv(){return String.join(",",escape(txId),escape(isbn),escape(memberId),escape(issueDate),escape(returnDate),String.valueOf(returned));}
    static Transaction fromCsv(String line){String[] f=Book.parse(line,6);return new Transaction(f[0],f[1],f[2],f[3],f[4],Boolean.parseBoolean(f[5]));}
    static String escape(String s){if(s.contains(",")||s.contains("\"")) return "\"" + s.replace("\"","\"\"") + "\""; return s;}
}

class Library {
    Map<String,Book> books=new LinkedHashMap<>();
    Map<String,Member> members=new LinkedHashMap<>();
    Map<String,Transaction> transactions=new LinkedHashMap<>();
    int nextTx=1;
    String booksFile="books.csv";
    String membersFile="members.csv";
    String txFile="transactions.csv";
    SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
    Library(){ensureFiles();loadAll();}
    void ensureFiles(){try{Files.createDirectories(Paths.get(".")); if(!Files.exists(Paths.get(booksFile))){ try(PrintWriter pw=new PrintWriter(booksFile)){pw.println("ISBN,Title,Author,Copies");}} if(!Files.exists(Paths.get(membersFile))){ try(PrintWriter pw=new PrintWriter(membersFile)){pw.println("MemberID,Name,Phone");}} if(!Files.exists(Paths.get(txFile))){ try(PrintWriter pw=new PrintWriter(txFile)){pw.println("TxID,ISBN,MemberID,IssueDate,ReturnDate,Returned");}} }catch(Exception e){}}
    void loadAll(){loadBooks();loadMembers();loadTransactions();}
    void loadBooks(){books.clear(); try(BufferedReader r=new BufferedReader(new FileReader(booksFile))){String line=r.readLine(); while((line=r.readLine())!=null){ if(line.trim().isEmpty()) continue; Book b=Book.fromCsv(line); books.put(b.isbn,b);} }catch(Exception e){}}
    void loadMembers(){members.clear(); try(BufferedReader r=new BufferedReader(new FileReader(membersFile))){String line=r.readLine(); while((line=r.readLine())!=null){ if(line.trim().isEmpty()) continue; Member m=Member.fromCsv(line); members.put(m.id,m);} }catch(Exception e){}}
    void loadTransactions(){transactions.clear(); int max=0; try(BufferedReader r=new BufferedReader(new FileReader(txFile))){String line=r.readLine(); while((line=r.readLine())!=null){ if(line.trim().isEmpty()) continue; Transaction t=Transaction.fromCsv(line); transactions.put(t.txId,t); try{int v=Integer.parseInt(t.txId.replaceAll("[^0-9]","")); if(v>max) max=v;}catch(Exception ex){} } }catch(Exception e){} nextTx=max+1;}
    void saveBooks(){try(PrintWriter pw=new PrintWriter(booksFile)){pw.println("ISBN,Title,Author,Copies"); for(Book b:books.values()) pw.println(b.toCsv());}catch(Exception e){}}
    void saveMembers(){try(PrintWriter pw=new PrintWriter(membersFile)){pw.println("MemberID,Name,Phone"); for(Member m:members.values()) pw.println(m.toCsv());}catch(Exception e){}}
    void saveTransactions(){try(PrintWriter pw=new PrintWriter(txFile)){pw.println("TxID,ISBN,MemberID,IssueDate,ReturnDate,Returned"); for(Transaction t:transactions.values()) pw.println(t.toCsv());}catch(Exception e){}}
    boolean addBook(Book b){ if(books.containsKey(b.isbn)) return false; books.put(b.isbn,b); saveBooks(); return true; }
    boolean addMember(Member m){ if(members.containsKey(m.id)) return false; members.put(m.id,m); saveMembers(); return true; }
    String issueBook(String isbn,String memberId){ Book b=books.get(isbn); Member m=members.get(memberId); if(b==null||m==null) return null; if(b.copies<=0) return null; String txId="TX"+String.format("%05d",nextTx++); String date=df.format(new Date()); Transaction t=new Transaction(txId,isbn,memberId,date); transactions.put(txId,t); b.copies--; saveBooks(); saveTransactions(); return txId; }
    boolean returnBook(String txId){ Transaction t=transactions.get(txId); if(t==null||t.returned) return false; Book b=books.get(t.isbn); if(b!=null) b.copies++; t.returnDate=df.format(new Date()); t.returned=true; saveBooks(); saveTransactions(); return true; }
    List<Book> listBooks(){ return new ArrayList<>(books.values()); }
    List<Member> listMembers(){ return new ArrayList<>(members.values()); }
    List<Transaction> listTransactions(){ return new ArrayList<>(transactions.values()); }
}

public class MiniLibraryApp {
    Library lib=new Library();
    JFrame frame;
    JTable booksTable,membersTable,txTable;
    DefaultTableModel booksModel,membersModel,txModel;
    public MiniLibraryApp(){ initUI(); refreshAll(); }
    void initUI(){
        frame=new JFrame("Mini Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900,600);
        JTabbedPane tabs=new JTabbedPane();
        JPanel pBooks=new JPanel(new BorderLayout());
        booksModel=new DefaultTableModel(new String[]{"ISBN","Title","Author","Copies"},0);
        booksTable=new JTable(booksModel);
        pBooks.add(new JScrollPane(booksTable),BorderLayout.CENTER);
        JPanel pb=new JPanel();
        JButton addBookBtn=new JButton("Add Book");
        addBookBtn.addActionListener(e->showAddBook());
        JButton delBookBtn=new JButton("Delete Selected");
        delBookBtn.addActionListener(e->deleteSelectedBook());
        pb.add(addBookBtn); pb.add(delBookBtn);
        pBooks.add(pb,BorderLayout.SOUTH);
        tabs.add("Books",pBooks);
        JPanel pMembers=new JPanel(new BorderLayout());
        membersModel=new DefaultTableModel(new String[]{"MemberID","Name","Phone"},0);
        membersTable=new JTable(membersModel);
        pMembers.add(new JScrollPane(membersTable),BorderLayout.CENTER);
        JPanel pm=new JPanel();
        JButton addMemBtn=new JButton("Add Member");
        addMemBtn.addActionListener(e->showAddMember());
        JButton delMemBtn=new JButton("Delete Selected");
        delMemBtn.addActionListener(e->deleteSelectedMember());
        pm.add(addMemBtn); pm.add(delMemBtn);
        pMembers.add(pm,BorderLayout.SOUTH);
        tabs.add("Members",pMembers);
        JPanel pTx=new JPanel(new BorderLayout());
        txModel=new DefaultTableModel(new String[]{"TxID","ISBN","MemberID","IssueDate","ReturnDate","Returned"},0);
        txTable=new JTable(txModel);
        pTx.add(new JScrollPane(txTable),BorderLayout.CENTER);
        JPanel pt=new JPanel();
        JButton issueBtn=new JButton("Issue Book");
        issueBtn.addActionListener(e->showIssueDialog());
        JButton retBtn=new JButton("Return Book");
        retBtn.addActionListener(e->showReturnDialog());
        pt.add(issueBtn); pt.add(retBtn);
        pTx.add(pt,BorderLayout.SOUTH);
        tabs.add("Transactions",pTx);
        frame.getContentPane().add(tabs,BorderLayout.CENTER);
        JPanel top=new JPanel();
        JButton refresh=new JButton("Refresh");
        refresh.addActionListener(e->refreshAll());
        top.add(refresh);
        frame.getContentPane().add(top,BorderLayout.NORTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    void refreshAll(){
        refreshBooks(); refreshMembers(); refreshTransactions();
    }
    void refreshBooks(){
        booksModel.setRowCount(0);
        for(Book b:lib.listBooks()) booksModel.addRow(new Object[]{b.isbn,b.title,b.author,b.copies});
    }
    void refreshMembers(){
        membersModel.setRowCount(0);
        for(Member m:lib.listMembers()) membersModel.addRow(new Object[]{m.id,m.name,m.phone});
    }
    void refreshTransactions(){
        txModel.setRowCount(0);
        for(Transaction t:lib.listTransactions()) txModel.addRow(new Object[]{t.txId,t.isbn,t.memberId,t.issueDate,t.returnDate,t.returned});
    }
    void showAddBook(){
        JTextField isbn=new JTextField(); JTextField title=new JTextField(); JTextField author=new JTextField(); JTextField copies=new JTextField();
        Object[] fields={"ISBN:",isbn,"Title:",title,"Author:",author,"Copies:",copies};
        int ok=JOptionPane.showConfirmDialog(frame,fields,"Add Book",JOptionPane.OK_CANCEL_OPTION);
        if(ok==JOptionPane.OK_OPTION){
            try{ Book b=new Book(isbn.getText().trim(),title.getText().trim(),author.getText().trim(),Integer.parseInt(copies.getText().trim())); if(lib.addBook(b)){refreshBooks(); JOptionPane.showMessageDialog(frame,"Added.");} else JOptionPane.showMessageDialog(frame,"ISBN exists."); }catch(Exception e){JOptionPane.showMessageDialog(frame,"Invalid input.");}
        }
    }
    void deleteSelectedBook(){
        int r=booksTable.getSelectedRow(); if(r<0) return; String isbn=(String)booksModel.getValueAt(r,0); int ok=JOptionPane.showConfirmDialog(frame,"Delete book "+isbn+"?","Confirm",JOptionPane.YES_NO_OPTION); if(ok==JOptionPane.YES_OPTION){ lib.books.remove(isbn); lib.saveBooks(); refreshBooks(); } }
    void showAddMember(){ JTextField id=new JTextField(); JTextField name=new JTextField(); JTextField phone=new JTextField(); Object[] fields={"Member ID:",id,"Name:",name,"Phone:",phone}; int ok=JOptionPane.showConfirmDialog(frame,fields,"Add Member",JOptionPane.OK_CANCEL_OPTION); if(ok==JOptionPane.OK_OPTION){ try{ Member m=new Member(id.getText().trim(),name.getText().trim(),phone.getText().trim()); if(lib.addMember(m)){refreshMembers(); JOptionPane.showMessageDialog(frame,"Added.");} else JOptionPane.showMessageDialog(frame,"Member ID exists."); }catch(Exception e){JOptionPane.showMessageDialog(frame,"Invalid input."); } } }
    void deleteSelectedMember(){ int r=membersTable.getSelectedRow(); if(r<0) return; String id=(String)membersModel.getValueAt(r,0); int ok=JOptionPane.showConfirmDialog(frame,"Delete member "+id+"?","Confirm",JOptionPane.YES_NO_OPTION); if(ok==JOptionPane.YES_OPTION){ lib.members.remove(id); lib.saveMembers(); refreshMembers(); } }
    void showIssueDialog(){ JTextField isbn=new JTextField(); JTextField mem=new JTextField(); Object[] f={"ISBN:",isbn,"Member ID:",mem}; int ok=JOptionPane.showConfirmDialog(frame,f,"Issue Book",JOptionPane.OK_CANCEL_OPTION); if(ok==JOptionPane.OK_OPTION){ String tx=lib.issueBook(isbn.getText().trim(),mem.getText().trim()); if(tx==null) JOptionPane.showMessageDialog(frame,"Issue failed (no book/member or no copies)."); else { refreshAll(); JOptionPane.showMessageDialog(frame,"Issued. TxID: "+tx); } } }
    void showReturnDialog(){ JTextField txid=new JTextField(); Object[] f={"Transaction ID:",txid}; int ok=JOptionPane.showConfirmDialog(frame,f,"Return Book",JOptionPane.OK_CANCEL_OPTION); if(ok==JOptionPane.OK_OPTION){ boolean s=lib.returnBook(txid.getText().trim()); if(s){ refreshAll(); JOptionPane.showMessageDialog(frame,"Returned."); } else JOptionPane.showMessageDialog(frame,"Return failed (invalid tx or already returned)."); } }
    public static void main(String[] args){ SwingUtilities.invokeLater(MiniLibraryApp::new); }
}
