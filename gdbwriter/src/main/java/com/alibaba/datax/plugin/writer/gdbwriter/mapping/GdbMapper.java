/**
 * 
 */
package net.tbsoft.datax.plugin.writer.gdbwriter.mapping;

import java.util.function.Function;

import net.tbsoft.datax.common.element.Record;
import net.tbsoft.datax.plugin.writer.gdbwriter.model.GdbElement;

/**
 * @author jerrywang
 *
 */
public interface GdbMapper {
    Function<Record, GdbElement> getMapper(MappingRule rule);
}
