import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * 4) Onde usar (e onde NÃO usar) Optional num código de verdade.
 *
 * O consenso da comunidade e a intenção original dos autores do JDK:
 * Optional é para TIPO DE RETORNO de método que pode não achar nada.
 * Fora disso, quase sempre atrapalha.
 */
public class OptionalNaPratica {

    // ======================================================================
    // O USO CORRETO: retorno de busca que pode não encontrar
    // ======================================================================

    record Usuario(long id, String nome, String email) {}

    static class UsuarioRepository {
        private final Map<Long, Usuario> banco = Map.of(
                1L, new Usuario(1, "Ana", "ana@exemplo.com"),
                2L, new Usuario(2, "Bruno", "bruno@exemplo.com"));

        /** A assinatura já avisa: pode não achar. Impossível ignorar. */
        Optional<Usuario> buscarPorId(long id) {
            return Optional.ofNullable(banco.get(id));
        }

        Optional<Usuario> buscarPorEmail(String email) {
            return banco.values().stream()
                    .filter(u -> u.email().equalsIgnoreCase(email))
                    .findFirst(); // findFirst já devolve Optional
        }

        List<Usuario> todos() {
            return List.copyOf(banco.values());
        }
    }

    static class UsuarioService {
        private final UsuarioRepository repo = new UsuarioRepository();

        /** Cada chamador decide o que fazer com o "não achei". */
        String nomeOuAnonimo(long id) {
            return repo.buscarPorId(id)
                    .map(Usuario::nome)
                    .orElse("anônimo");
        }

        Usuario obrigatorio(long id) {
            return repo.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("usuário " + id + " não existe"));
        }

        String dominioDoEmail(long id) {
            return repo.buscarPorId(id)
                    .map(Usuario::email)
                    .filter(e -> e.contains("@"))
                    .map(e -> e.substring(e.indexOf('@') + 1))
                    .orElse("desconhecido");
        }
    }

    public static void main(String[] args) {
        UsuarioService service = new UsuarioService();

        System.out.println("--- uso correto ---");
        System.out.println("nome id=1  : " + service.nomeOuAnonimo(1));
        System.out.println("nome id=99 : " + service.nomeOuAnonimo(99));
        System.out.println("domínio id=2: " + service.dominioDoEmail(2));

        try {
            service.obrigatorio(99);
        } catch (IllegalArgumentException e) {
            System.out.println("obrigatório: " + e.getMessage());
        }

        antipadroes();
        colecoes();
        primitivos();
    }

    // ======================================================================
    // OS ANTIPADRÕES
    // ======================================================================
    static void antipadroes() {
        System.out.println("\n--- o que NÃO fazer ---");

        // 1) Optional como CAMPO de classe
        //    Optional não é Serializable e adiciona um objeto por campo.
        //    Prefira o campo normal + getter que devolve Optional.
        //
        //      class Pedido {
        //          private Optional<String> obs;  // ruim
        //          private String obs;            // bom
        //          Optional<String> getObs() { return Optional.ofNullable(obs); }
        //      }

        // 2) Optional como PARÂMETRO de método
        //    Quem chama é obrigado a embrulhar tudo, e ainda pode passar null
        //    no lugar do Optional — você ganhou uma checagem, não perdeu.
        //
        //      void buscar(Optional<String> filtro)   // ruim
        //      void buscar(String filtro)             // bom (aceite null)
        //      void buscar()  +  void buscar(String)  // melhor ainda: sobrecarga

        // 3) Devolver null de um método que retorna Optional
        //    É o pior dos dois mundos. Se não tem valor, devolva Optional.empty().

        // 4) isPresent() + get() — já visto em ConsumindoOptional

        // 5) Chamar orElse(null) só para voltar ao null
        //    Às vezes é legítimo (interop com API antiga), mas se você faz isso
        //    logo depois de criar o Optional, ele não serviu para nada.

        // 6) Optional dentro de Optional — use flatMap
        Optional<Optional<String>> aninhado = Optional.of(Optional.of("x"));
        System.out.println("aninhado (evite): " + aninhado);
        System.out.println("achatado        : " + aninhado.flatMap(o -> o));

        // 7) Comparar Optional com equals sem pensar
        //    equals compara o CONTEÚDO, o que costuma ser o esperado — mas
        //    Optional.of("a").equals("a") é false (tipos diferentes).
        System.out.println("of(\"a\").equals(of(\"a\")) = " + Optional.of("a").equals(Optional.of("a")));
        System.out.println("of(\"a\").equals(\"a\")     = " + Optional.of("a").equals("a"));
    }

    // ======================================================================
    // Optional e coleções
    // ======================================================================
    static void colecoes() {
        System.out.println("\n--- coleções ---");

        // NÃO devolva Optional<List<T>>. Uma lista vazia já significa "nada".
        // Optional<List<T>> obriga o chamador a tratar dois "vazios" diferentes:
        // Optional.empty() e a lista vazia dentro dele.
        System.out.println("ruim: Optional<List<T>>  -> devolva List.of()");

        List<String> resultado = buscarNomes("zzz");
        System.out.println("busca sem resultado: " + resultado + " (isEmpty=" + resultado.isEmpty() + ")");

        // Do mesmo jeito, evite List<Optional<T>>: filtre antes.
        List<Optional<String>> ruim = List.of(Optional.of("a"), Optional.empty());
        List<String> bom = ruim.stream().flatMap(Optional::stream).toList();
        System.out.println("List<Optional> " + ruim + " -> " + bom);
    }

    static List<String> buscarNomes(String filtro) {
        List<String> encontrados = new ArrayList<>();
        for (String nome : List.of("Ana", "Bruno", "Carla")) {
            if (nome.toLowerCase().contains(filtro.toLowerCase())) {
                encontrados.add(nome);
            }
        }
        return encontrados; // lista vazia, nunca null, nunca Optional
    }

    // ======================================================================
    // Variantes primitivas
    // ======================================================================
    static void primitivos() {
        System.out.println("\n--- OptionalInt / OptionalDouble ---");

        List<Integer> notas = List.of(7, 9, 6);

        // Evitam o boxing de Optional<Integer>, mas têm API reduzida:
        // não têm map, flatMap nem filter.
        OptionalDouble media = notas.stream().mapToInt(Integer::intValue).average();
        System.out.println("média: " + media.orElse(0.0));

        // Como não têm map, se você precisa transformar, converta para Optional:
        String formatada = media.isPresent()
                ? Optional.of(media.getAsDouble()).map(m -> "%.2f".formatted(m)).orElseThrow()
                : "sem notas";
        System.out.println("formatada: " + formatada);

        // Na prática: use as variantes primitivas quando elas caem no seu colo
        // (retorno de IntStream), e Optional<T> no resto.
    }
}
