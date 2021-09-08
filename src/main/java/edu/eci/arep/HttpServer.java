package edu.eci.arep;

import java.net.*;
import java.io.*;
import java.util.HashMap;

public class HttpServer {
    public HttpServer(){}

    /*
     *   Método que inicializa el servidor
     * */
    public void start() throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println( "Listo para recibir ..." );
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println( "Accept failed." );
                System.exit( 1 );
            }
            serveConnection(clientSocket);
            clientSocket.close();

        }
        serverSocket.close();
    }

    /**
     * Metodo  procesar las peticiones.
     * @param clientSocket conexion Socket con cliente
     * @throws IOException Error de lectura del cliente socket
     */
    public void serveConnection (Socket clientSocket) throws IOException {
        OutputStream out= clientSocket.getOutputStream();
        BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
        String inputLine, outputLine;
        HashMap<String,String> request  = new HashMap<String,String>();
        boolean ready = false;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Received: " + inputLine);
            if(!ready){
                request.put("rq", inputLine);
                ready =true;
            } else {
                String[] line = inputLine.split( ":" );
                if (line.length>1 )
                    request.put( line[0], line[1] );
            }
            if (!in.ready()) {
                break;
            }
        }
        getResource(request,out);
        in.close();
    }

    /**
     * Genera la respuesta de la peticion del cliente
     * @param ClientSocket OutputStream del socket del cliente
     * @param response información procesada de la petición del cliente
     * @throws IOException Error de escritura del recurso
     */
    public  void getResource(HashMap<String,String> response, OutputStream ClientSocket) throws IOException {
        String[] requestLine = response.get( "rq" ).split( " " );
        if(!requestLine[1].contains(".")) {
            PrintWriter printWriter = new PrintWriter( ClientSocket, true );
            String city = requestLine[1];
            String path = city;
            String[] route = path.split( "\\/" );
            String city2 = route[route.length - 1];
            System.out.println( city2 );
            String answer = "";
            URL url = new URL( "https://api.openweathermap.org/data/2.5/weather?q=" + city2 + "&appid=602d8e6663a4731d5a708462db8af16b" );
            getResponse( url, printWriter );
            ClientSocket.close();
        }

    }
    private void getResponse(URL url, PrintWriter clientsocket) {
        HttpURLConnection connection = null;
        StringBuilder answer = new StringBuilder();
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod( "GET" );
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                    answer.append( line );
            }
            bufferedReader.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        if ( answer != null ) {
            String content = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html\r\n" + "\r\n"
                    + "<!DOCTYPE html>\n"
                    + "<html>\n" + "<head>\n" + "<meta charset=\"UTF-8\">\n"
                    + "<title></title>\n"
                    + "</head>\n"
                    + "<body>\n"
                    + "<p>" + answer + "</p>"
                    + "</body>\n"
                    + "</html>\n";
            clientsocket.println( content );
        } else {
            throw new NullPointerException();
        }
    }
}