import java.util.List;
import java.util.Optional;

/**
 * 3) map, flatMap, filter e or — encadeando sem desempacotar.
 *
 * A força do Optional está aqui: você descreve o que quer fazer COM o valor
 * e o "e se não tiver?" fica para o fim da cadeia, num lugar só.
 *
 * (Complementa os exemplos da pasta ../flatMap.)
 */
public class TransformandoOptional {

    record Cupom(String codigo, int percentual) {}

    record Cliente(String nome, String email, Optional<Cupom> cupom) {}

    public static void main(String[] args) {
        Cliente comCupom = new Cliente("Ana", "ana@exemplo.com",
                Optional.of(new Cupom("BLACK50", 50)));
        Cliente semCupom = new Cliente("Bruno", "bruno@exemplo.com", Optional.empty());

        // ------------------------------------------------------------------
        // map: transforma o conteúdo se existir
        // ------------------------------------------------------------------
        Optional<String> nomeMaiusculo = Optional.of(comCupom).map(c -> c.nome().toUpperCase());
        System.out.println("map -> " + nomeMaiusculo);

        // map sobre vazio não faz nada e não estoura — a lambda nem roda.
        Optional<String> deVazio = Optional.<Cliente>empty().map(c -> c.nome().toUpperCase());
        System.out.println("map sobre vazio -> " + deVazio);

        // Detalhe: se a função do map devolver null, o resultado é Optional.empty
        // (o map já faz ofNullable internamente).
        Optional<String> mapouParaNull = Optional.of("x").map(s -> (String) null);
        System.out.println("map que devolve null -> " + mapouParaNull);

        // ------------------------------------------------------------------
        // flatMap: quando a função JÁ devolve um Optional
        // ------------------------------------------------------------------
        // Cliente.cupom() é Optional<Cupom>. Com map daria Optional<Optional<Cupom>>.
        System.out.println("\nmap     -> " + Optional.of(comCupom).map(Cliente::cupom));
        System.out.println("flatMap -> " + Optional.of(comCupom).flatMap(Cliente::cupom));

        // ------------------------------------------------------------------
        // filter: esvazia o Optional se a condição falhar
        // ------------------------------------------------------------------
        System.out.println("\n--- filter ---");
        for (Cliente cliente : List.of(comCupom, semCupom)) {
            int desconto = Optional.of(cliente)
                    .flatMap(Cliente::cupom)             // tem cupom?
                    .filter(cp -> cp.percentual() >= 20) // é um cupom relevante?
                    .map(Cupom::percentual)              // pega o número
                    .orElse(0);                          // senão, sem desconto

            System.out.printf("  %-6s desconto: %d%%%n", cliente.nome(), desconto);
        }

        // ------------------------------------------------------------------
        // or: Optional alternativo (Java 9+)
        // ------------------------------------------------------------------
        // Diferente do orElse (que devolve o VALOR), o or devolve outro OPTIONAL,
        // então dá para encadear fontes de fallback e continuar na cadeia.
        System.out.println("\n--- or: cascata de fontes ---");
        String config = buscarNoAmbiente("TIMEOUT")
                .or(() -> buscarNoArquivo("TIMEOUT"))
                .or(() -> buscarNoPadrao("TIMEOUT"))
                .orElseThrow(() -> new IllegalStateException("TIMEOUT não configurado"));
        System.out.println("valor final: " + config);

        // ------------------------------------------------------------------
        // stream: a ponte para a Stream API (Java 9+)
        // ------------------------------------------------------------------
        System.out.println("\n--- Optional.stream ---");
        List<Cliente> clientes = List.of(comCupom, semCupom);

        // Optional.stream() vira um stream de 0 ou 1 elemento, então o flatMap
        // descarta os vazios sem precisar de filter + map + get.
        List<String> codigos = clientes.stream()
                .map(Cliente::cupom)
                .flatMap(Optional::stream)
                .map(Cupom::codigo)
                .toList();
        System.out.println("cupons ativos: " + codigos);

        // Versão antiga (antes do Java 9), só para comparar:
        List<String> antesDoJava9 = clientes.stream()
                .map(Cliente::cupom)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Cupom::codigo)
                .toList();
        System.out.println("mesma coisa, feia : " + antesDoJava9);

        // ------------------------------------------------------------------
        // Cadeia completa: imperativo vs declarativo
        // ------------------------------------------------------------------
        System.out.println("\n--- imperativo vs declarativo ---");
        System.out.println("declarativo: " + mensagemDeDesconto(comCupom));
        System.out.println("declarativo: " + mensagemDeDesconto(semCupom));
    }

    static String mensagemDeDesconto(Cliente cliente) {
        return cliente.cupom()
                .filter(cp -> cp.percentual() > 0)
                .map(cp -> "%s tem %d%% off com o código %s"
                        .formatted(cliente.nome(), cp.percentual(), cp.codigo()))
                .orElseGet(() -> cliente.nome() + " não tem cupom");

        // O equivalente imperativo, se cupom fosse um campo null:
        //
        //   Cupom cp = cliente.cupom();
        //   if (cp == null) return cliente.nome() + " não tem cupom";
        //   if (cp.percentual() <= 0) return cliente.nome() + " não tem cupom";
        //   return "...";
        //
        // Repare que o "não tem cupom" aparece duas vezes lá e uma só aqui:
        // a cadeia junta todos os caminhos de falha num ponto único.
    }

    // Fontes de configuração fictícias para o exemplo do `or`.
    static Optional<String> buscarNoAmbiente(String chave) {
        System.out.println("  consultando variáveis de ambiente...");
        return Optional.empty();
    }

    static Optional<String> buscarNoArquivo(String chave) {
        System.out.println("  consultando arquivo de config...");
        return Optional.empty();
    }

    static Optional<String> buscarNoPadrao(String chave) {
        System.out.println("  usando padrão embutido...");
        return Optional.of("30s");
    }
}
