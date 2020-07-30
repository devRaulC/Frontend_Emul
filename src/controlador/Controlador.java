package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import vista.Vista;
import java.util.List;
import javax.swing.JOptionPane;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import modelo.*;

/**
 *
 * @author Raúl
 */
public class Controlador implements ActionListener, MouseListener {

    private final Vista vista;
    private final ButtonGroup grupoBotones;
    Icon icon;
    Juego juego;
    SessionFactory sesionFactory;
    Session session;
    Transaction tx;

    //CONSTRUCTOR
    public Controlador() {
        vista = new Vista();
        grupoBotones = vista.getButtonGroup1();
        Iniciador();
    }

    //INICIA LOS OYENTES DE LOS BOTONES DEL FORMULARIO Y MUESTRA EL FORMULARIO
    public void Iniciador() {

        vista.getBtnAñadir().addActionListener(this);
        vista.getBtnModificar().addActionListener(this);
        vista.getBtnEliminar().addActionListener(this);
        vista.getBtnLanzar().addActionListener(this);
        vista.setVisible(true);

        RellenarTabla();
    }

    //REFRESCA EL CONTENIDO DE LA TABLA DE JUEGOS DEPENDIENDO DEL SISTEMA ELEGIDO
    public void RellenarTabla() {
        sesionFactory = NewHibernateUtil.getSessionFactory();
        session = sesionFactory.openSession();

        //LISTA DE JUEGOS POR CRITERIOS PARA REALIZAR CONSULTAS
        List<Juego> listaJuego = session.createCriteria(Juego.class).list();

        //MODELO DE LA TABLA, LE APLICA UNA CABECERA
        DefaultTableModel dtm = new DefaultTableModel();
        vista.getTablaJuegos().setModel(dtm);
        dtm.addColumn("idJuego");
        dtm.addColumn("titulo");
        dtm.addColumn("consola");
        dtm.addColumn("genero");
        dtm.addColumn("nJugadores");

        //RECORRE LOS RESULTADOS DE LA LISTA DE JUEGO Y LOS MUESTRA EN LA TABLA
        for (int i = 0; i < listaJuego.size(); i++) {
            dtm.addRow(new Object[]{
                listaJuego.get(i).getIdjuego(),
                listaJuego.get(i).getTitulo(),
                listaJuego.get(i).getConsola(),
                listaJuego.get(i).getGenero(),
                listaJuego.get(i).getNjugadores()});
        }
    }

    //AÑADE UN JUEGO A LA BASE DE DATOS
    public void AñadirJuego(Session session) {
        sesionFactory = NewHibernateUtil.getSessionFactory();
        session = sesionFactory.openSession();
        tx = session.beginTransaction();

        //CREA UN NUEVO OBJETO Y LE APLICA LOS VALORES QUE TENGA EN LA VISTA
        Juego nuevoJuego = new Juego();
        nuevoJuego.setIdjuego(Integer.parseInt(vista.getTxtID().getText()));
        nuevoJuego.setTitulo(vista.getTxtTitulo().getText());
        nuevoJuego.setConsola(vista.getTxtConsola().getText());
        nuevoJuego.setGenero(vista.getTxtGenero().getText());
        nuevoJuego.setNjugadores(vista.getTxtNJugadores().getText());

        //INSERT
        session.save(nuevoJuego);
        //EJECUTA LA TRANSACCIÓN
        tx.commit();
        RellenarTabla();
        JOptionPane.showMessageDialog(null, "El juego se ha insertado correctamente");

    }

    //MODIFICA EL REGISTRO MARCADO EN LA CASILLA ID
    public void ModificarJuego(Session session) {
        sesionFactory = NewHibernateUtil.getSessionFactory();
        session = sesionFactory.openSession();
        tx = session.beginTransaction();

        //RECIBE UN ID Y LE CAMBIA EL RESTO DE LOS VALORES POR LOS QUE TIENE EN LA VISTA
        Juego juegoModificado = (Juego) session.get(Juego.class, Integer.parseInt(vista.getTxtID().getText()));
        juegoModificado.setTitulo(vista.getTxtTitulo().getText());
        juegoModificado.setConsola(vista.getTxtConsola().getText());
        juegoModificado.setGenero(vista.getTxtGenero().getText());
        juegoModificado.setNjugadores(vista.getTxtNJugadores().getText());

        //UPDATE
        session.saveOrUpdate(juegoModificado);
        //EJECUTA LA TRANSACCIÓN
        tx.commit();
        RellenarTabla();
        JOptionPane.showMessageDialog(null, "El juego se ha modificado correctamente");

    }

    //ELIMINA EL REGISTRO MARCADO EN LA CASILLA ID
    public void EliminarJuego(Session session) {
        sesionFactory = NewHibernateUtil.getSessionFactory();
        session = sesionFactory.openSession();
        tx = session.beginTransaction();

        //RECIBE UN ID Y ELIMINA ELIMINA ESE REGISTRO 
        Juego eliminar = (Juego) session.get(Juego.class, Integer.parseInt(vista.getTxtID().getText()));

        //DELETE
        session.delete(eliminar);
        //EJECUTA LA TRANSACCIÓN
        tx.commit();
        RellenarTabla();
        JOptionPane.showMessageDialog(null, "El juego se ha eliminado correctamente");
    }

    //LANZA EL EMULADOR SELECCIONADO Y MUESTRA UNA MINIATURA DE LA CONSOLA
    public void LanzarEmulador() throws IOException {
        Runtime app = Runtime.getRuntime();

        //AÑADE BOTONES A LA LISTA
        grupoBotones.add(vista.getMegaDrive());
        grupoBotones.add(vista.getSuperNintendo());

        //ACCIÓN DE LOS BOTONES QUE DEVUELVE LA RUTA LOCAL DE LAS IMAGENES
        vista.getMegaDrive().setActionCommand("md.jpg");
        vista.getSuperNintendo().setActionCommand("sns.jpg");

        String seleccionado = grupoBotones.getSelection().getActionCommand();

        //CAMBIAR IMAGEN
        JLabel imagen = vista.getJlblIcono();
        imagen.setIcon(new ImageIcon("C:/Users/Agares/Documents/NetBeansProjects/ProyectoFrontend/build/classes/imagenes/" + seleccionado));

        //CAMBIAR EMULADOR DEPENDIENDO DE LA IMAGEN
        String emulador = null;
        String path = "C:/Users/Agares/Documents/NetBeansProjects/ProyectoFrontend/build/classes/emuladores/";

        if (seleccionado == "md.jpg") {
            emulador = "Fusion.exe";
        } else if (seleccionado == "sns.jpg") {
            emulador = "snes9x.exe";
        }

        //LANZAR EL EMULADOR
        try {
            app.exec(path + emulador);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //ASIGNACIÓN DE FUNCIONES A LOS BOTONES DE LA VISTA
    @Override
    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()) {

            case "Añadir":
                AñadirJuego(session);
                break;

            case "Modificar":
                ModificarJuego(session);
                break;

            case "Eliminar":
                EliminarJuego(session);
                break;

            case "Lanzar": {
                try {
                    LanzarEmulador();
                } catch (IOException ex) {
                    Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
