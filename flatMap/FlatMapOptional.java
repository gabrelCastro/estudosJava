import java.util.List;
import java.util.Optional;

/**
 * 3) flatMap não é só de Stream — Optional também tem.
 *
 * A ideia é a mesma: evitar o "container dentro de container".
 * Em Optional, map devolve Optional<Optional<T>> quando a função já
 * devolve um Optional. O flatMap achata para Optional<T>.
 */
public class FlatMapOptional {

    record Endereco(String rua, String cidade) {}

    record Empresa(String nome, Optional<Endereco> endereco) {}

    record Funcionario(String nome, Optional<Empresa> empresa) {}

    public static void main(String[] args) {
        Funcionario comTudo = new Funcionario("Ana",
                Optional.of(new Empresa("Acme",
                        Optional.of(new Endereco("Rua A, 100", "Recife")))));

        Funcionario semEndereco = new Funcionario("Bruno",
                Optional.of(new Empresa("Globex", Optional.empty())));

        Funcionario desempregado = new Funcionario("Carla", Optional.empty());

        List<Funcionario> funcionarios = List.of(comTudo, semEndereco, desempregado);

        // --- O problema com map ---
        // funcionario.empresa() já é um Optional<Empresa>, então map devolve
        // Optional<Optional<Empresa>>. Horrível de usar.
        Optional<Optional<Empresa>> aninhado = Optional.of(comTudo).map(Funcionario::empresa);
        System.out.println("map     -> " + aninhado);

        // --- Com flatMap ---
        Optional<Empresa> achatado = Optional.of(comTudo).flatMap(Funcionario::empresa);
        System.out.println("flatMap -> " + achatado);

        // --- Encadeando vários níveis sem um único if != null ---
        System.out.println("\ncidade de cada funcionário:");
        for (Funcionario f : funcionarios) {
            String cidade = Optional.of(f)
                    .flatMap(Funcionario::empresa)   // Optional<Empresa>
                    .flatMap(Empresa::endereco)      // Optional<Endereco>
                    .map(Endereco::cidade)           // map porque cidade() é String pura
                    .orElse("desconhecida");

            System.out.println("  " + f.nome() + ": " + cidade);
        }

        // Compare com a versão imperativa que isso substitui:
        //
        //   String cidade = "desconhecida";
        //   if (f != null && f.empresa() != null) {
        //       Empresa e = f.empresa();
        //       if (e.endereco() != null) {
        //           cidade = e.endereco().cidade();
        //       }
        //   }

        // --- Regra prática ---
        // A função devolve um Optional?  -> flatMap
        // A função devolve o valor cru?  -> map
        //
        // Vale igual pra Stream:
        // A função devolve um Stream/coleção? -> flatMap
        // A função devolve um valor só?       -> map

        // --- Ponte entre Optional e Stream ---
        // Optional.stream() devolve um stream de 0 ou 1 elemento, o que
        // combina perfeitamente com flatMap para descartar os vazios.
        List<String> empresasExistentes = funcionarios.stream()
                .flatMap(f -> f.empresa().stream())   // some quem não tem empresa
                .map(Empresa::nome)
                .toList();

        System.out.println("\nempresas: " + empresasExistentes);
    }
}
