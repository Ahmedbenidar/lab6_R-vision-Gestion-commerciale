package org.example.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.example.service.RapportVentes;

/**
 * Servlet simple qui expose le rapport des ventes du mois precedent.
 * URL : /rapport-ventes
 */
@WebServlet("/rapport-ventes")
public class RapportVentesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final RapportVentes rapportVentes = new RapportVentes();

    @Override
    protected void doGet(HttpServletRequest requete, HttpServletResponse reponse)
            throws ServletException, IOException {

        LocalDate aujourdhui = LocalDate.now();
        LocalDate debut = aujourdhui.minusMonths(1).withDayOfMonth(1);
        LocalDate fin = debut.withDayOfMonth(debut.lengthOfMonth());

        Map<String, BigDecimal> rapport =
                rapportVentes.chiffreAffairesParCategorie(debut, fin);

        reponse.setContentType("text/html;charset=UTF-8");
        PrintWriter sortie = reponse.getWriter();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        sortie.println("<html><head><title>Rapport des ventes</title></head><body>");
        sortie.println("<h1>Chiffre d'affaires par categorie</h1>");
        sortie.println("<p>Periode : " + debut.format(format) + " -> " + fin.format(format) + "</p>");
        sortie.println("<table border='1' cellpadding='6'>");
        sortie.println("<tr><th>Categorie</th><th>Chiffre d'affaires</th></tr>");

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> ligne : rapport.entrySet()) {
            sortie.println("<tr><td>" + ligne.getKey() + "</td><td>" + ligne.getValue() + "</td></tr>");
            total = total.add(ligne.getValue());
        }
        sortie.println("<tr><th>Total</th><th>" + total + "</th></tr>");
        sortie.println("</table></body></html>");
    }
}
