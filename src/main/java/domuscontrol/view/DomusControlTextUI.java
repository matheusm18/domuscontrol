package domuscontrol.view;

import domuscontrol.DomusControlController;
import domuscontrol.menu.NewMenu;
import domuscontrol.exceptions.LoginInvalidPasswordException;
import domuscontrol.exceptions.UserAlreadyExistsException;
import domuscontrol.exceptions.UserNotLoggedInException;
import domuscontrol.exceptions.UserNotFoundException;
import domuscontrol.model.device.Curtain;
import domuscontrol.model.device.Device;
import domuscontrol.model.device.Gate;
import domuscontrol.model.device.Lamp;
import domuscontrol.model.device.Plug;
import domuscontrol.model.device.Relay;
import domuscontrol.model.device.Speaker;
import domuscontrol.model.houses.House;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DomusControlTextUI {

    private static final String SAVES_DIR = "saves";

    private final DomusControlController controller;
    private final Scanner sc;

    public DomusControlTextUI(DomusControlController controller) {
        this.controller = controller;
        this.sc = new Scanner(System.in);
    }

    public void run() {
        NewMenu menu = new NewMenu(new String[]{
            "Login",
            "Registar",
            "Minhas Casas",
            "O meu perfil",
            "Estatísticas",
            "Avançar simulação",
            "Guardar estado",
            "Carregar estado",
            "Terminar sessão"
        });

        menu.setPreCondition(1, () -> !controller.isLoggedIn());
        menu.setPreCondition(2, () -> !controller.isLoggedIn());
        menu.setPreCondition(3, () ->  controller.isLoggedIn());
        menu.setPreCondition(4, () ->  controller.isLoggedIn());
        menu.setPreCondition(5, () ->  controller.isLoggedIn());
        menu.setPreCondition(6, () ->  controller.isLoggedIn());
        menu.setPreCondition(7, () ->  controller.isLoggedIn());
        menu.setPreCondition(9, () ->  controller.isLoggedIn());

        menu.setHandler(1, () -> doLogin());
        menu.setHandler(2, () -> doRegister());
        menu.setHandler(3, () -> minhasCasas());
        menu.setHandler(4, () -> meuPerfil());
        menu.setHandler(5, () -> estatisticas());
        menu.setHandler(6, () -> avancarSimulacao());
        menu.setHandler(7, () -> guardarEstado());
        menu.setHandler(8, () -> carregarEstado());
        menu.setHandler(9, () -> doLogout());

        menu.run();
    }

    // -- Autenticação ----------------------------------------------

    private void doLogin() {
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();
        try {
            controller.login(email, password);
            System.out.println("Bem-vindo/a, " + controller.getSessionName() + "!");
        } catch (UserNotFoundException e) {
            System.out.println("Utilizador não encontrado.");
        } catch (LoginInvalidPasswordException e) {
            System.out.println("Password incorreta.");
        } catch (UserNotLoggedInException e) {
            System.out.println("Erro interno.");
        }
    }

    private void doRegister() {
        System.out.print("Nome: ");
        String name = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();
        try {
            controller.register(name, email, password);
            System.out.println("Conta criada com sucesso!");
        } catch (UserAlreadyExistsException e) {
            System.out.println("Já existe uma conta com esse email.");
        }
    }

    private void doLogout() {
        controller.logout();
        System.out.println("Sessão terminada.");
    }

    // -- Casas -----------------------------------------------------

    private void minhasCasas() {
        NewMenu menu = new NewMenu(new String[]{
            "Listar as minhas casas",
            "Criar nova casa",
            "Seleccionar casa"
        });

        menu.setHandler(1, () -> listarCasas());
        menu.setHandler(2, () -> criarCasa());
        menu.setHandler(3, () -> seleccionarCasa());

        menu.run();
    }

    private void listarCasas() {
        try {
            List<House> casas = controller.getMyHouses();
            if (casas.isEmpty()) {
                System.out.println("Não tens nenhuma casa.");
                return;
            }
            casas.forEach(h -> System.out.println("[" + h.getId() + "] " + h.getName()));
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void criarCasa() {
        System.out.print("Nome da casa: ");
        String nome = sc.nextLine();
        try {
            controller.createHouse(nome);
            System.out.println("Casa criada com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void seleccionarCasa() {
        listarCasas();
        System.out.print("ID da casa: ");
        try {
            int id = Integer.parseInt(sc.nextLine());
            House casa = controller.getHouse(id);
            menuCasa(id, casa.getName());
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void menuCasa(int houseId, String nomeCasa) {
        NewMenu menu = new NewMenu(new String[]{
            "Ver detalhes da casa",
            "Gerir divisões",
            "Gerir dispositivos"
        });

        menu.setPreCondition(2, () -> isAdmin(houseId));
        menu.setPreCondition(3, () -> isAdmin(houseId));

        menu.setHandler(1, () -> verCasa(houseId));
        menu.setHandler(2, () -> gerirDivisoes(houseId));
        menu.setHandler(3, () -> gerirDispositivos(houseId));

        System.out.println("\n=== " + nomeCasa + " ===");
        menu.run();
    }

    private boolean isAdmin(int houseId) {
        try {
            return controller.isAdminOfHouse(houseId);
        } catch (Exception e) {
            return false;
        }
    }

    private void verCasa(int houseId) {
        try {
            System.out.println(controller.getHouse(houseId));
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // -- Divisões --------------------------------------------------

    private void gerirDivisoes(int houseId) {
        NewMenu menu = new NewMenu(new String[]{
            "Adicionar divisão",
            "Remover divisão"
        });

        menu.setHandler(1, () -> adicionarDivisao(houseId));
        menu.setHandler(2, () -> removerDivisao(houseId));

        menu.run();
    }

    private void adicionarDivisao(int houseId) {
        System.out.print("Nome da divisão: ");
        String nome = sc.nextLine();
        try {
            controller.addDivision(houseId, nome);
            System.out.println("Divisão adicionada.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void removerDivisao(int houseId) {
        try {
            House casa = controller.getHouse(houseId);
            casa.getDivisions().keySet().forEach(d -> System.out.println("- " + d));
            System.out.print("Nome da divisão a remover: ");
            String nome = sc.nextLine();
            controller.removeDivision(houseId, nome);
            System.out.println("Divisão removida.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // -- Dispositivos ----------------------------------------------

    private void gerirDispositivos(int houseId) {
        NewMenu menu = new NewMenu(new String[]{
            "Adicionar dispositivo",
            "Listar dispositivos"
        });

        menu.setHandler(1, () -> adicionarDispositivo(houseId));
        menu.setHandler(2, () -> listarDispositivos(houseId));

        menu.run();
    }

    private void listarDispositivos(int houseId) {
        try {
            House casa = controller.getHouse(houseId);
            Map<Integer, Device> dispositivos = casa.getDevices();
            if (dispositivos.isEmpty()) {
                System.out.println("Nenhum dispositivo nesta casa.");
                return;
            }
            dispositivos.values().forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void adicionarDispositivo(int houseId) {
        try {
            House casa = controller.getHouse(houseId);
            if (casa.getDivisions().isEmpty()) {
                System.out.println("Cria primeiro uma divisão.");
                return;
            }
            casa.getDivisions().keySet().forEach(d -> System.out.println("- " + d));
            System.out.print("Divisão onde adicionar: ");
            String divisao = sc.nextLine();

            if (!casa.getDivisions().containsKey(divisao)) {
                System.out.println("Divisão '" + divisao + "' não existe.");
                return;
            }

            NewMenu tipoMenu = new NewMenu(new String[]{
                "Lâmpada (Lamp)",
                "Coluna (Speaker)",
                "Cortina (Curtain)",
                "Portão (Gate)",
                "Tomada (Plug)",
                "Relé (Relay)"
            });

            tipoMenu.setHandler(1, () -> adicionarLamp(houseId, divisao));
            tipoMenu.setHandler(2, () -> adicionarSpeaker(houseId, divisao));
            tipoMenu.setHandler(3, () -> adicionarCurtain(houseId, divisao));
            tipoMenu.setHandler(4, () -> adicionarGate(houseId, divisao));
            tipoMenu.setHandler(5, () -> adicionarPlug(houseId, divisao));
            tipoMenu.setHandler(6, () -> adicionarRelay(houseId, divisao));

            tipoMenu.run();
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private String[] lerDadosBase() {
        System.out.print("Marca: ");
        String brand = sc.nextLine();
        System.out.print("Modelo: ");
        String model = sc.nextLine();
        System.out.print("Consumo (Wh/h): ");
        String consumption = sc.nextLine();
        return new String[]{brand, model, consumption};
    }

    private void adicionarLamp(int houseId, String divisao) {
        try {
            String[] base = lerDadosBase();
            System.out.print("Brilho inicial (0-100): ");
            int brightness = Integer.parseInt(sc.nextLine());
            System.out.print("Temperatura de cor (2700-4000 K): ");
            int colorTemp = Integer.parseInt(sc.nextLine());
            Lamp lamp = new Lamp(base[0], base[1], Double.parseDouble(base[2]), brightness, colorTemp);
            controller.addDeviceToDivision(houseId, lamp, divisao);
            System.out.println("Lâmpada adicionada.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void adicionarSpeaker(int houseId, String divisao) {
        try {
            String[] base = lerDadosBase();
            System.out.print("Volume inicial (0-100): ");
            int volume = Integer.parseInt(sc.nextLine());
            System.out.print("Fonte de áudio: ");
            String source = sc.nextLine();
            Speaker speaker = new Speaker(base[0], base[1], Double.parseDouble(base[2]), volume, source);
            controller.addDeviceToDivision(houseId, speaker, divisao);
            System.out.println("Coluna adicionada.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void adicionarCurtain(int houseId, String divisao) {
        try {
            String[] base = lerDadosBase();
            System.out.print("Abertura inicial (0-100%): ");
            int opening = Integer.parseInt(sc.nextLine());
            Curtain curtain = new Curtain(base[0], base[1], Double.parseDouble(base[2]), opening);
            controller.addDeviceToDivision(houseId, curtain, divisao);
            System.out.println("Cortina adicionada.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void adicionarGate(int houseId, String divisao) {
        try {
            String[] base = lerDadosBase();
            System.out.print("Abertura inicial (0-100%): ");
            int opening = Integer.parseInt(sc.nextLine());
            Gate gate = new Gate(base[0], base[1], Double.parseDouble(base[2]), opening);
            controller.addDeviceToDivision(houseId, gate, divisao);
            System.out.println("Portão adicionado.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void adicionarPlug(int houseId, String divisao) {
        try {
            String[] base = lerDadosBase();
            Plug plug = new Plug(base[0], base[1], Double.parseDouble(base[2]));
            controller.addDeviceToDivision(houseId, plug, divisao);
            System.out.println("Tomada adicionada.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void adicionarRelay(int houseId, String divisao) {
        try {
            String[] base = lerDadosBase();
            Relay relay = new Relay(base[0], base[1], Double.parseDouble(base[2]));
            controller.addDeviceToDivision(houseId, relay, divisao);
            System.out.println("Relé adicionado.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // -- Perfil ----------------------------------------------------

    private void meuPerfil() {
        NewMenu menu = new NewMenu(new String[]{
            "Ver perfil",
            "Alterar nome",
            "Alterar email",
            "Alterar password"
        });

        menu.setHandler(1, () -> verPerfil());
        menu.setHandler(2, () -> alterarNome());
        menu.setHandler(3, () -> alterarEmail());
        menu.setHandler(4, () -> alterarPassword());

        menu.run();
    }

    private void verPerfil() {
        try {
            System.out.println("Email: " + controller.getSessionEmail());
            System.out.println("Nome:  " + controller.getSessionName());
        } catch (UserNotLoggedInException e) {
            System.out.println("Não está logado.");
        }
    }

    private void alterarNome() {
        System.out.print("Novo nome: ");
        String nome = sc.nextLine();
        try {
            controller.updateName(nome);
            System.out.println("Nome alterado.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void alterarEmail() {
        System.out.print("Novo email: ");
        String email = sc.nextLine();
        try {
            controller.updateEmail(email);
            System.out.println("Email alterado.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void alterarPassword() {
        System.out.print("Nova password: ");
        String pwd = sc.nextLine();
        try {
            controller.updatePassword(pwd);
            System.out.println("Password alterada.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // -- Estatísticas ----------------------------------------------

    private void estatisticas() {
        House casa = controller.getMostConsumingHouse();
        if (casa == null) {
            System.out.println("Nenhuma casa no sistema.");
        } else {
            System.out.println("Casa com maior consumo: " + casa.getName() + " [ID: " + casa.getId() + "]");
            System.out.printf("Consumo total: %.2f Wh%n", casa.calculateTotalConsumption());
        }
    }

    // -- Simulação / Persistência ----------------------------------

    private void avancarSimulacao() {
        System.out.print("Minutos a avançar: ");
        try {
            int minutos = Integer.parseInt(sc.nextLine());
            controller.advanceTime(minutos);
            System.out.println("Simulação avançada " + minutos + " minuto(s).");
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido.");
        }
    }

    private void guardarEstado() {
        System.out.print("Nome do ficheiro: ");
        String ficheiro = sc.nextLine();
        try {
            new File(SAVES_DIR).mkdirs();
            String caminho = SAVES_DIR + File.separator + ficheiro;
            controller.saveState(caminho);
            System.out.println("Estado guardado em '" + caminho + "'.");
        } catch (IOException e) {
            System.out.println("Erro ao guardar: " + e.getMessage());
        }
    }

    private void carregarEstado() {
        System.out.print("Nome do ficheiro: ");
        String ficheiro = sc.nextLine();
        try {
            String caminho = SAVES_DIR + File.separator + ficheiro;
            controller.loadState(caminho);
            System.out.println("Estado carregado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao carregar: " + e.getMessage());
        }
    }
}