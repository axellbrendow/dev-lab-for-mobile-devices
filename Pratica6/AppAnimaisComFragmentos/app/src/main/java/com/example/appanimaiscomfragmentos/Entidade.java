package com.example.appanimaiscomfragmentos;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Entidade<ENTIDADE>
{
    protected Tabela tabela;
    private Class<ENTIDADE> classe;
    private Constructor<ENTIDADE> constructor;

    /**
     * Este HashMap deverá guardar chaves e valores da seguinte forma:
     * <br>
     * chave: nomeColuna, valor: valorColuna.
     * <br><br>
     * Em outras palavras, ele será capaz de, dado o nome de uma coluna na tabela,
     * obter o valor do atributo do objeto que correspondete a essa coluna.
     * <br><br>
     * Ex.: Supondo um objeto com o atributo "nomeObj" e supondo que a coluna
     * equivalente na tabela desse objeto tem o nome "nome", ao fazer
     * valores.get("nome"), o HashMap deverá retornar o valor atual do atributo
     * "nomeObj" do objeto.
     */
    private HashMap<String, Object> valores;

    /**
     * Este HashMap deverá guardar chaves e valores da seguinte forma:
     * <br>
     * chave: nomeColuna, valor: nomeAtributo.
     * <br><br>
     * Em outras palavras, ele será capaz de, dado o nome da coluna na tabela,
     * obter o nome do atributo equivalente do objeto.
     * <br><br>
     * Ex.: Supondo um objeto com o atributo "nomeObj" e supondo que a coluna
     * equivalente na tabela desse objeto tem o nome "nome", ao fazer
     * valores.get("nome"), o HashMap deverá retornar "nomeObj".
     */
    private HashMap<String, String> colunaParaAtributo;

    // ---------------------- Construtores

    /**
     * Use este construtor caso os nomes dos atributos da sua classe forem diferentes
     * dos nomes das colunas da tabela no banco de dados.
     *
     * @param tabela             Tabela da sua entidade.
     * @param classe             Classe da sua entidade.
     * @param colunaParaAtributo Este HashMap deverá guardar chaves e valores
     * da seguinte forma:
     * <br>
     * chave: nomeColuna, valor: nomeAtributo.
     * <br><br>
     * Em outras palavras, ele será capaz de, dado o nome da coluna na tabela,
     * obter o nome do atributo equivalente do objeto.
     * <br><br>
     * Ex.: Supondo um objeto com o atributo "nomeObj" e supondo que a coluna
     * equivalente na tabela desse objeto tem o nome "nome", ao fazer
     * valores.get("nome"), o HashMap deverá retornar "nomeObj".
     */
    public Entidade(Tabela tabela, Class<ENTIDADE> classe, HashMap<String, String> colunaParaAtributo)
    {
        this.tabela = tabela;
        this.classe = classe;
        this.valores = new HashMap<>();

        // null indica que o nome da coluna é igual ao nome do atributo da classe
        if (colunaParaAtributo == null)
        {
            colunaParaAtributo = new HashMap<>();

            for (String nomeColuna : this.tabela.getNomesColunas())
            {
                // Nesse caso, o nome da coluna será o mesmo do atributo do objeto
                colunaParaAtributo.put(nomeColuna, nomeColuna);
            }
        }

        this.colunaParaAtributo = colunaParaAtributo;

        try
        {
            this.constructor = classe.getConstructor();
        }

        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Use este construtor caso os nomes dos atributos da sua classe forem diferentes
     * dos nomes das colunas da tabela no banco de dados.
     *
     * @param tabela             Tabela da sua entidade.
     * @param classe             Classe da sua entidade.
     * @param colunaParaAtributo Este HashMap deverá guardar chaves e valores
     * da seguinte forma:
     * <br>
     * chave: nomeColuna, valor: nomeAtributo.
     * <br><br>
     * Em outras palavras, ele será capaz de, dado o nome da coluna na tabela,
     * obter o nome do atributo equivalente do objeto.
     * <br><br>
     * Ex.: Supondo um objeto com o atributo "nomeObj" e supondo que a coluna
     * equivalente na tabela desse objeto tem o nome "nome", ao fazer
     * valores.get("nome"), o HashMap deverá retornar "nomeObj".
     */
    public Entidade(String tabela, Class<ENTIDADE> classe, HashMap<String, String> colunaParaAtributo)
    {
        this(MainActivity.sgbd.obterTabela(tabela), classe, colunaParaAtributo);
    }

    /**
     * Use este construtor caso os nomes dos atributos da sua classe forem iguais
     * aos nomes das colunas da tabela no banco de dados.
     *
     * @param tabela             Tabela da sua entidade.
     * @param classe             Classe da sua entidade.
     */
    public Entidade(String tabela, Class<ENTIDADE> classe)
    {
        this(tabela, classe, null);
    }

    // ---------------------- Métodos auxiliares

    public Object obterChavePrimaria()
    {
        return getAttributeOf(this, tabela.obterNomeDaChavePrimaria());
    }

    /**
     * Primeiro, converte o nome da coluna para o nome do atributo correspondente na
     * entidade. Daí, acessa esse atributo na entidade e obtém o valor.
     *
     * @param entidade Entidade cujo atributo deseja-se obter.
     * @param nomeDaColuna Nome da coluna na tabela que corresponde a esse atributo.
     *
     * @return {@code null} caso a entidade não tenha o atributo. Caso contrário,
     * retorna o valor do atributo.
     */
    private Object getAttributeOf(Entidade<ENTIDADE> entidade, String nomeDaColuna)
    {
        String nomeDoAtributo = colunaParaAtributo.get(nomeDaColuna);
        Field campo = null;
        Object atributo = null;

        try
        {
            campo = classe.getDeclaredField(nomeDoAtributo); // Obtém o atributo
            // Guarda o valor atual da acessibilidade
            boolean acessivel = campo.isAccessible();

            campo.setAccessible(true);
            atributo = campo.get(entidade);
            campo.setAccessible(acessivel);
        }

        catch (NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return atributo;
    }

    /**
     * Gera um HashMap que associa o nome da coluna na tabela com o valor atual do
     * atributo desta entidade.
     * <br><br>
     * Ex.: Supondo que esta entidade tenha o atributo "nomeEnt" e a coluna
     * correspondente na tabela tenha o nome "nome". Ao chamar gerarValores("nome"),
     * este método retornará um HashMap com a chave "nome" associada ao valor atual
     * do atributo "nomeEnt".
     *
     * @param colunas Colunas da tabela que deseja-se extrair desta entidade.
     *
     * @return um HashMap que associa o nome da coluna na tabela com o valor atual do
     * atributo desta entidade.
     */
    private HashMap<String, Object> gerarValores(String... colunas)
    {
        if (colunas == null || colunas.length == 0) colunas = tabela.getNomesColunas();

        for (String coluna : colunas)
        {
            valores.put(coluna, getAttributeOf(this, coluna));
        }

        return valores;
    }

    /**
     * Primeiro, converte o nome da coluna para o nome do atributo correspondente na
     * entidade. Daí, acessa esse atributo na entidade e define-o com o valor da coluna.
     *
     * @param entidade Entidade cujo atributo deseja-se definir.
     * @param nomeDaColuna Nome da coluna na tabela que corresponde a esse atributo.
     */
    private void setAttributeOf(ENTIDADE entidade, String nomeDaColuna, String valorDaColuna)
    {
        String nomeDoAtributo = colunaParaAtributo.get(nomeDaColuna);
        String tipoDaColuna = tabela.colunaParaTipo.get(nomeDaColuna);
        Field campo = null;

        try
        {
            campo = classe.getDeclaredField(nomeDoAtributo); // Obtém o atributo
            // Guarda o valor atual da acessibilidade
            boolean acessivel = campo.isAccessible();

            campo.setAccessible(true);

            switch (tipoDaColuna)
            {
                case Tabela.TIPO_INTEIRO:
                    campo.setInt(entidade, Integer.parseInt(valorDaColuna));
                    break;

                case Tabela.TIPO_TEXTO:
                    campo.set(entidade, valorDaColuna);
                    break;

                case Tabela.TIPO_REAL:
                    campo.setDouble(entidade, Double.parseDouble(valorDaColuna));
                    break;
            }

            campo.setAccessible(acessivel);
        }

        catch (NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Converte a tupla com os valores das entidades em um objeto real dessa entidade.
     *
     * @param tupla   Arranjo com os valores das colunas na tabela.
     * @param colunas Colunas correspondentes.
     *
     * @return entidade tendo apenas os atributos de interesse populados.
     */
    private ENTIDADE converterTupla(String[] tupla, String... colunas)
    {
        ENTIDADE entidade = null;

        if (tupla != null && tupla.length > 0 && colunas != null)
        {
            try
            {
                entidade = constructor.newInstance();

                for (int j = 0; j < colunas.length; j++)
                {
                    setAttributeOf(entidade, colunas[j], tupla[j]);
                }
            }

            catch (IllegalAccessException | InstantiationException |
                InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }

        return entidade;
    }

    /**
     * Converte as tuplas com os valores das entidades em objetos reais dessas
     * entidades.
     *
     * @param tuplas  Matriz onde as linhas representam as tuplas das entidades
     *                e as colunas representam os valores das colunas na tabela.
     * @param colunas Colunas correspondentes.
     *
     * @return ArrayList com as entidades tendo apenas os atributos de interesse
     * populados.
     */
    private ArrayList<ENTIDADE> converterTuplas(String[][] tuplas, String... colunas)
    {
        ArrayList<ENTIDADE> entidades = new ArrayList<>();

        for (int i = 0; i < tuplas.length; i++)
        {
            entidades.add(converterTupla(tuplas[i], colunas));
        }

        return entidades;
    }

    // ---------------------- Métodos com queries SQL

    /**
     * Exclui a tabela desta entidade do banco de dados.
     */
    public void dropTable()
    {
        tabela.drop();
    }

    /**
     * Faz o SELECT no banco de dados com as cláusulas e condições estabelecidas
     * extraindo apenas as colunas informadas.
     * Caso nenhuma coluna for informada, todas as colunas serão recuperadas.
     *
     * @param clausulasECondicoes Cláusulas e condições do SELECT. (ex.: "WHERE id = 7").
     * @param colunas Colunas a serem extraídas.
     *
     * @return ArrayList com as entidades tendo apenas os atributos de interesse
     * populados.
     */
    public ArrayList<ENTIDADE> select(String clausulasECondicoes, String... colunas)
    {
        return converterTuplas( tabela.select(clausulasECondicoes, colunas) );
    }

    /**
     * Faz o SELECT no banco de dados extraindo apenas as colunas informadas.
     * Caso nenhuma coluna for informada, todas as colunas serão recuperadas.
     *
     * @param colunas Colunas a serem extraídas.
     *
     * @return ArrayList com as entidades tendo apenas os atributos de interesse
     * populados.
     */
    public ArrayList<ENTIDADE> select(String... colunas)
    {
        return select("", colunas);
    }

    /**
     * Faz o SELECT no banco de dados com a cláusula WHERE e condições estabelecidas
     * extraindo apenas as colunas informadas.
     * Caso nenhuma coluna for informada, todas as colunas serão recuperadas.
     *
     * @param condicoes Condições do SELECT WHERE. (ex.: "id = 7").
     * @param colunas Colunas a serem extraídas.
     *
     * @return ArrayList com as entidades tendo apenas os atributos de interesse
     * populados.
     */
    public ArrayList<ENTIDADE> where(String condicoes, String... colunas)
    {
        return select("WHERE " + condicoes, colunas);
    }

    /**
     * Seleciona a entidade com a chave primária informada.
     *
     * @param primaryKey Valor da chave primária.
     *
     * @return  {@code null} caso não for encontrada uma entidade com a chave primária
     * informada. Caso contrário, retorna a entidade com a chave primária informada.
     */
    public ENTIDADE selectByPK(Object primaryKey)
    {
        return converterTupla(tabela.selectByPK(primaryKey), tabela.getNomesColunas());
    }

    /**
     * Exclui entidades no banco filtradas pelas cláusulas e condições estabelecidas.
     *
     * @param clausulasECondicoes Cláusulas e condições da query DELETE.
     *                            (ex.: "WHERE id = 7").
     */
    public void delete(String clausulasECondicoes)
    {
        tabela.delete(clausulasECondicoes);
    }

    /**
     * Exclui entidades no banco filtradas pela cláusula WHERE e as condições
     * estabelecidas.
     *
     * @param condicoes Condições da cláusula WHERE. (ex.: "id = 7").
     */
    public void deleteWhere(String condicoes)
    {
        delete("WHERE " + condicoes);
    }

    /**
     * Exclui a entidade no banco filtrada pela cláusula WHERE e pela chave primária
     * sendo o valor recebido. Só utilize este método se a chave primária for única
     * e não composta.
     *
     * @param primaryKey Valor da chave primária da tupla.
     */
    public void deleteByPK(Object primaryKey)
    {
        tabela.deleteByPK(primaryKey);
    }

    /**
     * Exclui esta entidade do banco de dados. Só utilize este método se a chave
     * primária for única e não composta.
     */
    public void delete()
    {
        deleteByPK( obterChavePrimaria() );
    }

    /**
     * Insere esta entidade no banco de dados.
     */
    public void insert()
    {
        tabela.insert( tabela.obterStringParaInsert( gerarValores() ) );
    }

    /**
     * Filtra as tuplas de acordo com as cláusulas e condições estabelecidas e então
     * atualiza as colunas recebidas com os valores passados.
     *
     * @param clausulasECondicoes Cláusulas e condições da query UPDATE.
     *                            (ex.: "WHERE id = 7").
     * @param colunasEValores     Colunas e valores a serem atualizados.
     *                            (ex.: "nome = 'myname', cidade = 'mycity'").
     */
    public void update(String clausulasECondicoes, String colunasEValores)
    {
        tabela.update(clausulasECondicoes, colunasEValores);
    }

    /**
     * Filtra as tuplas de acordo com as cláusulas e condições estabelecidas e então
     * atualiza as colunas recebidas com os valores passados.
     *
     * @param condicoes       Condições da cláusula WHERE. (ex.: "id = 7").
     * @param colunasEValores Colunas e valores a serem atualizados.
     *                        (ex.: "nome = 'myname', cidade = 'mycity'").
     */
    public void updateWhere(String condicoes, String colunasEValores)
    {
        update("WHERE " + condicoes, colunasEValores);
    }

    /**
     * Atualiza esta entidade no banco com os valores atuais dos seus atributos.
     * Só utilize este método se a chave primária for única e não composta.
     */
    public void update()
    {
        tabela.updateByPK(
            obterChavePrimaria(), tabela.obterStringParaUpdate(gerarValores()));
    }
}
