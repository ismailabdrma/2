package com.example.stage2025.mapper;

import com.example.stage2025.dto.SoapFieldDto;
import com.example.stage2025.dto.SoapOperationMeta;
import com.example.stage2025.dto.SupplierDto;
import com.example.stage2025.entity.ApiSupplier;
import com.example.stage2025.entity.ExcelSupplier;
import com.example.stage2025.entity.SoapField;
import com.example.stage2025.entity.SoapSupplier;
import com.example.stage2025.entity.SoapSupplierOperationMeta;
import com.example.stage2025.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierMapper INSTANCE = Mappers.getMapper(SupplierMapper.class);

    @Mapping(target = "apiUrl", expression = "java(supplier instanceof ApiSupplier ? ((ApiSupplier) supplier).getApiUrl() : null)")
    @Mapping(target = "wsdlUrl", expression = "java(supplier instanceof SoapSupplier ? ((SoapSupplier) supplier).getWsdlUrl() : null)")
    @Mapping(target = "excelSheetName", expression = "java(supplier instanceof ExcelSupplier ? ((ExcelSupplier) supplier).getExcelSheetName() : null)")
    @Mapping(target = "csvDelimiter", expression = "java(supplier.getDataFormat() == com.example.stage2025.enums.DataFormat.CSV ? ((com.example.stage2025.entity.CsvSupplier) supplier).getCsvDelimiter() : null)")
    @Mapping(source = "soapOperationMeta", target = "soapOperationMeta", qualifiedByName = "mapSoapOperationMetaToDto")
    SupplierDto toDto(Supplier supplier);

    @Named("mapSoapOperationMetaToDto")
    default SoapOperationMeta mapSoapOperationMetaToDto(SoapSupplierOperationMeta meta) {
        if (meta == null) {
            return null;
        }
        SoapOperationMeta dto = new SoapOperationMeta();
        dto.setOperationName(meta.getOperationName());
        dto.setSoapAction(meta.getSoapAction());
        dto.setInputElement(meta.getInputElement());
        dto.setOutputElement(meta.getOutputElement());
        dto.setInputFields(mapSoapFieldsToDto(meta.getInputFields()));
        dto.setOutputFields(mapSoapFieldsToDto(meta.getOutputFields()));
        // Complex types are not directly mapped from entity as they are dynamic from WSDL
        return dto;
    }

    default List<SoapFieldDto> mapSoapFieldsToDto(List<SoapField> fields) {
        if (fields == null) {
            return null;
        }
        return fields.stream()
                .map(field -> {
                    SoapFieldDto dto = new SoapFieldDto();
                    dto.setName(field.getName());
                    dto.setType(field.getType());
                    dto.setComplex(field.isComplex());
                    dto.setArray(field.isArray());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // This mapping from DTO to Entity will need to be handled in the service layer
    // because it involves creating specific subclass instances (ApiSupplier, SoapSupplier, etc.)
    // based on the dataFormat. MapStruct cannot dynamically create subclasses.
    // So, this method is intentionally left abstract or ignored for direct DTO-to-Entity mapping.
    // The service will handle the creation and population of the correct Supplier subclass.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataFormat", ignore = true) // Handled by subclass creation
    @Mapping(target = "lastImport", ignore = true)
    @Mapping(target = "active", ignore = true) // Handled by service
    Supplier toEntity(SupplierDto supplierDto);

    default List<SoapField> mapSoapFieldDtosToEntity(List<SoapFieldDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(dto -> new SoapField(dto.getName(), dto.getType(), dto.isComplex(), dto.isArray()))
                .collect(Collectors.toList());
    }
}
